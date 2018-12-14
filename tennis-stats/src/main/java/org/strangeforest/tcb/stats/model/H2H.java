package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.model.core.*;

public final class H2H {

	public static final H2H EMPTY = new H2H(0.0, 0.0);
	
	private final double won1;
	private final double won2;

	public H2H(WonLost wonLost) {
		this(wonLost.getWon(), wonLost.getLost());
	}

	public H2H(double won1, double won2) {
		this.won1 = won1;
		this.won2 = won2;
	}

	public double getWon1() {
		return won1;
	}

	public double getWon2() {
		return won2;
	}

	public double getTotal() {
		return won1 + won2;
	}

	public H2H add(H2H h2h) {
		return new H2H(won1 + h2h.won1, won2 + h2h.won2);
	}

	public H2H scale(double value) {
		return new H2H(won1 * value, won2 * value);
	}
}
