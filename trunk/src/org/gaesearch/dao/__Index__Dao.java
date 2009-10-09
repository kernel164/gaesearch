package org.gaesearch.dao;

import java.util.List;
import java.util.Map;

import org.gaesearch.model.jdo.__Index__;

import com.google.common.collect.Multimap;

public interface __Index__Dao extends __Generic__Dao<String, __Index__> {
	List<String> getAllTokens();
	Multimap<String, Long> getIndexes(List<String> tokens);
	Map<String, Multimap<String, Long>> getIndexMap(List<String> tokens);
	Multimap<String, Long> getIndexesByPrefixes(String... prefixes);
	Map<String, Multimap<String, Long>> getIndexMapByPrefixes(String... prefixes);
	//Multimap<String, Long> getIndexesByPrefixes(List<String> tokens);
}
