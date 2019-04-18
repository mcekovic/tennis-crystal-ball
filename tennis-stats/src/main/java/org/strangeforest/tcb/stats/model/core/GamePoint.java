package org.strangeforest.tcb.stats.model.core;

public enum GamePoint {
	
	P0("0"),
	P15("15"),
	P30("30"),
	P40("40"),
	AD("AD");

	private final String text;

	GamePoint(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
