package org.strangeforest.tcb.stats.model;

public class Option {

	private final String value;
	private final String text;

	public Option(String value, String text) {
		this.value = value;
		this.text = text;
	}

	public String getValue() {
		return value;
	}

	public String getText() {
		return text;
	}
}
