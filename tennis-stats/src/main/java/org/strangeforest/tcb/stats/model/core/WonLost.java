package org.strangeforest.tcb.stats.model.core;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class WonLost implements Comparable<WonLost> {

	public static final WonLost EMPTY = new WonLost(0, 0);

	protected final int won;
	protected final int lost;
	protected final int total;
	protected double wonPct;

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

	public String pctWL() {
		return getWonPctStr(1) + " - " + getLostPctStr(1);
	}

	public String getWonPctStr(int decimals) {
		return total > 0 ? formatPct(wonPct, decimals) : "";
	}

	public String getLostPctStr(int decimals) {
		return total > 0 ? formatPct(PCT - wonPct, decimals) : "";
	}

	private static String formatPct(double pct, int decimals) {
		return format("%." + decimals + "f%%", pct);
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

	public String getWT() {
		return total > 0 ? won + " / " + total : "";
	}

	public String getPctWL() {
		return total > 0 ? getWonPctStr(1) + " (" + won + "-" + lost + ")" : "";
	}

	public String pctWT() {
		return total > 0 ? getWonPctStr(1) + " (" + won + "/" + total + ")" : "";
	}

	public WonLost inverted() {
		return new WonLost(lost, won, total);
	}

	public WonLost add(WonLost wonLost) {
		return new WonLost(won + wonLost.won, lost + wonLost.lost, total + wonLost.total);
	}


	// Object methods

	@Override public int compareTo(WonLost wonLost) {
		return Double.compare(wonLost.wonPct, wonPct);
	}
}
