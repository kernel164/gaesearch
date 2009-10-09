package org.gaesearch.model;

import java.util.Collections;
import java.util.Map;

public class SearchCapableMetaData {
	private String name;
	private String searchKeyName;
	private Map<String, String> searchFieldNames;

	public String getSearchKeyName() {
		return searchKeyName;
	}

	public void setSearchKeyName(String searchKeyName) {
		this.searchKeyName = searchKeyName;
	}

	public Map<String, String> getSearchFieldNames() {
		return searchFieldNames;
	}

	public void setSearchFieldNames(Map<String, String> fieldNames) {
		this.searchFieldNames = Collections.unmodifiableMap(fieldNames);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
