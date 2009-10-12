package org.gaesearch.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.gaesearch.dao.SearchIndexDao;
import org.gaesearch.model.jdo.SearchIndex;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class SearchIndexDaoImpl extends GenericDaoImpl<String, SearchIndex> implements SearchIndexDao {
	public SearchIndexDaoImpl() {
		super(SearchIndex.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllTokens() {
		PersistenceManager pm = pmf.getPersistenceManager();
		List<String> results = new ArrayList<String>();
		try {
			// TODO: use FetchPlan;
			List<String> values = (List<String>) pm.newQuery("SELECT token FROM " + clazz.getName()).execute();
			Iterator<String> it = values.iterator();
			while (it.hasNext()) {
				results.add(it.next());
			}
		} finally {
			pm.close();
		}
		return Collections.unmodifiableList(results);
	}

	@Override
	public Multimap<String, Long> getIndexes(List<String> ids) {
		Multimap<String, Long> multiMap = HashMultimap.create();
		int len = ids.size();
		for (int i = 0; i < len; i++) {
			String id = ids.get(i);
			SearchIndex index = get(id);
			if (index != null) {
				Multimap<String, Long> m = index.getContent();
				System.out.println("getIndexes: " + m);
				multiMap.putAll(m);
			}
		}
		return multiMap;
	}

	@Override
	public Map<String, Multimap<String, Long>> getIndexMap(List<String> ids) {
		// Multimap<String, Long> multiMap = HashMultimap.create();
		Map<String, Multimap<String, Long>> result = new HashMap<String, Multimap<String, Long>>();
		int len = ids.size();
		for (int i = 0; i < len; i++) {
			String id = ids.get(i);
			SearchIndex index = get(id);
			if (index != null) {
				// multiMap.putAll(index.getContent());
				result.put(id, index.getContent());
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
    @Override
	public Multimap<String, Long> getIndexesByPrefixes(String... prefixes) {
		PersistenceManager pm = pmf.getPersistenceManager();
		Query query = pm.newQuery(SearchIndex.class);
		query.setFilter(this.getStartsWith("token", prefixes));
		Multimap<String, Long> multiMap = HashMultimap.create();
		// String q = "select from " + __Index__.class.getName() + " where " +
		// getStartsWith("name", prefixes);
		try {
			List<SearchIndex> indexes = (List<SearchIndex>) query.execute();
			Iterator<SearchIndex> it = indexes.iterator();
			while (it.hasNext()) {
				SearchIndex index = it.next();
				Multimap<String, Long> m = index.getContent();
				System.out.println("getIndexesByPrefixes: " + m);
				multiMap.putAll(m);
			}
		} finally {
			pm.close();
		}

		return multiMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Multimap<String, Long>> getIndexMapByPrefixes(String... prefixes) {
		PersistenceManager pm = pmf.getPersistenceManager();
		Query query = pm.newQuery(SearchIndex.class);
		query.setFilter(getStartsWith("token", prefixes));
		Map<String, Multimap<String, Long>> result = new HashMap<String, Multimap<String, Long>>();
		// String q = "select from " + __Index__.class.getName() + " where " +
		// getStartsWith("name", prefixes);
		try {
			List<SearchIndex> indexes = (List<SearchIndex>) query.execute();
			Iterator<SearchIndex> it = indexes.iterator();
			while (it.hasNext()) {
				SearchIndex index = it.next();
				Multimap<String, Long> m = index.getContent();
				System.out.println("getIndexesByPrefixes: " + m);
				result.put(index.getToken(), m);
			}
		} finally {
			pm.close();
		}

		return result;
	}
}

/*
 * public List<String> getAllKeys() { DatastoreService ds =
 * DatastoreServiceFactory.getDatastoreService(); Query query = new
 * Query("__Index__"); //query.addFilter("__key__", FilterOperator.GREATER_THAN,
 * KeyFactory.stringToKey(a_key_string)); query.setKeysOnly();
 *
 * List<Entity> qResults = ds.prepare(query).asList(withOffset(0)); List<String>
 * results = new ArrayList<String>(); int len = qResults.size(); for (int i = 0;
 * i < len; i++) {
 * results.add(KeyFactory.keyToString(qResults.get(i).getKey())); }
 *
 * return Collections.unmodifiableList(results); }
 */
