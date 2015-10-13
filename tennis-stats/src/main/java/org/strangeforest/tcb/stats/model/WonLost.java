package org.strangeforest.tcb.stats.model;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class WonLost {

	private final int won;
	private final int lost;
	private final int total;
	private final double wonPct;

	public WonLost(int won, int lost) {
		this.won = won;
		this.lost = lost;
		total = won + lost;
		wonPct = pct(won, total);
	}

	public int getWon() {
		return won;
	}

	public int getLost() {
		return lost;
	}

	public int getTotal() {
		return total;
	}

	public double getWonPct() {
		return wonPct;
	}

	public boolean isEmpty() {
		return total == 0;
	}

	@Override public String toString() {
		return won + " - " + lost + " / " + total;
	}
}
