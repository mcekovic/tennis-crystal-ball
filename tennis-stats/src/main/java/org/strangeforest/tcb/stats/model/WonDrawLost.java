package org.strangeforest.tcb.stats.model;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class WonDrawLost extends WonLost {

	private final int draw;

	public WonDrawLost(int won, int draw, int lost) {
		this(won, draw, lost, won + draw + lost);
	}

	public WonDrawLost(int won, int draw, int lost, int total) {
		super(won, lost, total);
		this.draw = draw;
		wonPct = pct(2 * won + draw, 2 * (won + draw + lost));
	}

	public int getDraw() {
		return draw;
	}

	public WonDrawLost inverted() {
		return new WonDrawLost(lost, draw, won, total);
	}
}
