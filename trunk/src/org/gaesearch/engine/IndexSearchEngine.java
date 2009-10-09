package org.gaesearch.engine;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManagerFactory;

import org.gaesearch.annotation.SearchCapable;

public interface IndexSearchEngine {
	String OBJECT = "__GET__OBJECT__";
	String PMF = "__GET__PMF__";

	void index(Map<String, Object> settings);
	void unIndex(Map<String, Object> settings);

	List<Long> search(Map<String, Object> settings);

	List<String> getAllIndexTokens(Map<String, Object> settings);

	//Map<String, ?> getIndexMap(Map<String, Object> settings);

	Class<? extends SearchCapable>[] getSearchCapableClasses();

	void setPersistenceManagerFactory(PersistenceManagerFactory pmf);
}
