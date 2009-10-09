package org.gaesearch.engine.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gaesearch.annotation.SearchCapable;
import org.gaesearch.dao.__Index__Dao;
import org.gaesearch.dao.impl.__Index__DaoImpl;
import org.gaesearch.engine.IndexSearchEngine;
import org.gaesearch.model.SearchBy;
import org.gaesearch.model.SearchCapableMetaData;
import org.gaesearch.model.jdo.__Index__;
import org.gaesearch.util.GSrchUtils;

import com.google.common.collect.Multimap;

//http://coding-masters.blogspot.com/2009/09/make-reflection-as-fast-as-direct-calls.html
public class GIndexSearchEngine implements IndexSearchEngine {
	private Log logger = LogFactory.getLog(GIndexSearchEngine.class);
	private final Map<Class<? extends SearchCapable>, SearchCapableMetaData> annotationMap;
	private __Index__Dao indexDao = new __Index__DaoImpl();

	public GIndexSearchEngine(String scanPath) {
		this(new String[] {scanPath});
	}

	public GIndexSearchEngine(String[] scanPath) {
		logger.info("scanPath: " + Arrays.toString(scanPath));
		this.annotationMap = GSrchUtils.getMap(scanPath);
	}

	@Override
	public void setPersistenceManagerFactory(PersistenceManagerFactory pmf) {
		indexDao.setPersistenceManagerFactory(pmf);
	}

	@Override
	public void index(Map<String, Object> settings) {
		indexOrUnIndex(settings, true);
	}

	@Override
	public void unIndex(Map<String, Object> settings) {
		indexOrUnIndex(settings, false);
	}

	@Override
	public List<Long> search(Map<String, Object> settings) {
		Object obj = settings.get(IndexSearchEngine.OBJECT);

		if (obj instanceof String) {
			List<String> tokens = getTokens((String) obj);
			if (!tokens.isEmpty()) {
				return new ArrayList<Long>(indexDao.getIndexes(tokens).values());
			}
		}
		else if (obj instanceof SearchBy) {
			SearchBy searchBy = ((SearchBy) obj);
			List<String> tokens = getTokens(searchBy.getText());
			if (!tokens.isEmpty()) {
				// get by key..
				//Multimap<String, Long> indexes = indexDao.getIndexes(tokens);

				// get by prefix..
				//indexes.putAll(indexDao.getIndexesByPrefixes(tokens.toArray(new String[tokens.size()])));
				Multimap<String, Long> indexes = indexDao.getIndexesByPrefixes(tokens.toArray(new String[tokens.size()]));

				String type = searchBy.getType();
				if (StringUtils.isNotBlank(type)) {
					return new ArrayList<Long>(indexes.get(type));
				}
				return new ArrayList<Long>(indexes.values());
			}
		}
		/*
		else {
			// get model class;
			Class<?> clazz = obj.getClass();
			SearchCapableMetaData searchModel = annotationMap.get(clazz);
			if (searchModel != null) {
				Map<String, String> fieldNames = searchModel.getSearchFieldNames();
				Field[] fields = clazz.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					String fieldName = fields[i].getName();
					if (fieldNames.containsKey(fieldName)) {
						String text = GSrchUtils.getValue(obj, fields[i]).toString();
						if (StringUtils.isNotBlank(text)) {
							// String catKey = searchModel.getName() + '.' +
							// fieldNames.get(fieldName);

							// get all;
						}
					}
				}
			}
		}
		*/
		return Collections.emptyList();
	}

	@Override
	public List<String> getAllIndexTokens(Map<String, Object> settings) {
		return indexDao.getAllTokens();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends SearchCapable>[] getSearchCapableClasses() {
		Set<Class<? extends SearchCapable>> keySet = this.annotationMap.keySet();
		return keySet.toArray(new Class[keySet.size()]);
	}

	private List<String> getTokens(String text) {
		if (StringUtils.isNotBlank(text)) {
			List<String> tokenList = GSrchUtils.getTokens(text);
			List<String> removeTokens = new ArrayList<String>();

			for (String token : tokenList) {
				if (StringUtils.isNotBlank(token)) {
					if (token.length() <= 2) {
						removeTokens.add(token);
					}
				} else {
					removeTokens.add(token);
				}
			}

			if (!removeTokens.isEmpty()) {
				tokenList.removeAll(removeTokens);
			}
			return tokenList;
		}
		return Collections.emptyList();
	}

	private void indexOrUnIndex(Map<String, Object> settings, boolean isIndexing) {
		Object obj = settings.get(IndexSearchEngine.OBJECT);

		// get model class;
		Class<?> clazz = obj.getClass();
		SearchCapableMetaData searchModel = annotationMap.get(clazz);
		if (searchModel != null) {
			// get all the declared fields
			Field[] fields = clazz.getDeclaredFields();

			if (fields.length > 0) {
				Long refId = null; // will be lazy init below.
				// the Search Field map.
				Map<String, String> fieldNames = searchModel.getSearchFieldNames();
				// holds the __Index__ items to be saved / updated.
				Map<String, __Index__> toSave = new HashMap<String, __Index__>();
				// holds the __Index__ items to be deleted (mostly for unindexing)
				Map<String, __Index__> toDelete = new HashMap<String, __Index__>();

				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];

					// get field name;
					String fieldName = field.getName();

					// check if the fieldname annotated with SearchField.
					if (fieldNames.containsKey(fieldName)) {
						// lazy init refId;
						if (refId == null) {
							refId = (Long) GSrchUtils.getIdValue(obj, searchModel.getSearchKeyName());
						}

						// get the text to be indexed.
						String text = (String) GSrchUtils.getValue(obj, field);

						// check for non blank
						if (StringUtils.isNotBlank(text)) {
							// do indexing or unindexing
							doIndexingOrUnIndexing(text, searchModel.getName() + '.' + fieldNames.get(fieldName), refId, isIndexing, toSave, toDelete);
						}
					}
				}

				if (!toDelete.isEmpty()) {
					// remove index from save map which are present in delete map.
					toSave.keySet().removeAll(toDelete.keySet());

					// delete indexes.
					indexDao.removeAll(toDelete.values());
				}
				if (!toSave.isEmpty()) {
					// store indexes;
					indexDao.saveAll(toSave.values());
				}
			}
		}
	}

	private void doIndexingOrUnIndexing(String text, String tokenType, Long refId, boolean isIndexing, Map<String, __Index__> toSave, Map<String, __Index__> toDelete) {
		List<String> tokenList = getTokens(text);
		for (String token : tokenList) {
			// if unindexing and if token is already put for deletion, simply goto next token.
			if (!isIndexing && toDelete.containsKey(token)) {
				continue;
			}

			// get index for token from local cache
			__Index__ index = toSave.get(token);

			// if not found, get it from the Datastore.
			if (index == null) {
				try {
					index = indexDao.get(token);
				} catch (JDOObjectNotFoundException e) {
					// ignore; e.printStackTrace();
					System.out.println(e.getMessage());
				}
			}

			// if found, update.
			if (index != null) {
				boolean isTypeExists = index.typeExists(tokenType);
				boolean isRefExists = index.referenceExists(refId);

				if (isIndexing) {
					// if already present, no indexing
					if (isTypeExists && isRefExists) {
						continue;
					}
					// if not present, then do indexing.
					else {
						index.addContent(tokenType, refId);
						toSave.put(token, index);
					}
				}
				else {
					// if already present then remove.
					if (isTypeExists && isRefExists) {
						index.removeContent(tokenType, refId);
						if (index.isEmpty()) {
							toDelete.put(token, index);
						}
						else {
							toSave.put(token, index);
						}
					}
					//else if (isRefExists) {
					// TODO
					//}
				}
			} else {
				if (isIndexing) { // if not found (create new)
					index = new __Index__();
					index.setToken(token);
					index.addContent(tokenType, refId);
					toSave.put(token, index);
				}
			}
		}
	}
}


/*
You could use a TreeSet with a Comparator, either instead of a HashSet or as an intermediary.

Set<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
set.add("HELLO");
System.out.println(set);
set.remove("hello");
System.out.println(set);
*/