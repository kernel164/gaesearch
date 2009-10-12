package org.gaesearch.dao;

import java.util.List;
import java.util.Map;

import org.gaesearch.model.jdo.SearchIndex;

import com.google.common.collect.Multimap;

public interface SearchIndexDao extends GenericDao<String, SearchIndex> {
	List<String> getAllTokens();
	Multimap<String, Long> getIndexes(List<String> tokens);
	Map<String, Multimap<String, Long>> getIndexMap(List<String> tokens);
	Multimap<String, Long> getIndexesByPrefixes(String... prefixes);
	Map<String, Multimap<String, Long>> getIndexMapByPrefixes(String... prefixes);
	//Multimap<String, Long> getIndexesByPrefixes(List<String> tokens);
}
