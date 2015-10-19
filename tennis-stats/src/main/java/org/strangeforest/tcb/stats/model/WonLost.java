package org.strangeforest.tcb.stats.model;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class WonLost {

	public static final WonLost EMPTY = new WonLost(0, 0);

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

	public String getWonPctStr() {
		return getWonPctStr(1);
	}

	public String getWonPctStr(int decimals) {
		return total > 0 ? format(format("%%%1$d.%2$df%%%%", 4 + decimals, decimals), wonPct) : "";
	}

	public boolean isEmpty() {
		return total == 0;
	}

	public String getWLT() {
		return total > 0 ? won + " - " + lost + " / " + total : "";
	}

	public String getWL() {
		return total > 0 ? won + "-" + lost : "";
	}
}
