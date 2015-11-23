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
		this(won, lost, won + lost);
	}

	public WonLost(int won, int lost, int total) {
		this.won = won;
		this.lost = lost;
		this.total = total;
		wonPct = pct(won, won + lost);
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

	public int getWonPctClass() {
		if (wonPct <= 0.0)
			return 0;
		else if (wonPct < 20.0)
			return 10;
		else if (wonPct < 40.0)
			return 30;
		else if (wonPct < 60.0)
			return 50;
		else if (wonPct < 80.0)
			return 70;
		else if (wonPct < 100.0)
			return 90;
		else
			return 100;
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

	public WonLost inverted() {
		return new WonLost(lost, won, total);
	}

	public WonLost add(WonLost wonLost) {
		return new WonLost(won + wonLost.won, lost + wonLost.lost, total + wonLost.total);
	}
}
