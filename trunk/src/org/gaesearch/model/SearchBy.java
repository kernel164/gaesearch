package org.gaesearch.model;

public class SearchBy {
	private final String type;
	private final String text;

	public SearchBy(Class<?> clazz, String type, String text) {
		this.text = text;
		this.type = (type == null) ? null : clazz.getSimpleName() + '.' + type;
	}

	public String getType() {
		return type;
	}

	public String getText() {
		return text;
	}
}
