package org.strangeforest.tcb.stats.model;

public class GOATListRow {

	private int goatRanking;
	private String name;
	private int goatPoints;

	public GOATListRow(int goatRanking, String name, int goatPoints) {
		this.goatRanking = goatRanking;
		this.name = name;
		this.goatPoints = goatPoints;
	}

	public int getGoatRanking() {
		return goatRanking;
	}

	public String getName() {
		return name;
	}

	public int getGoatPoints() {
		return goatPoints;
	}
}
