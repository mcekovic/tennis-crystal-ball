package org.strangeforest.tcb.stats.model.prediction;

import java.util.function.*;

public abstract class DiffOutcome {

	private int items;
	private int itemsDiff;
	private Function<Integer, Double> pItemWin;

	protected DiffOutcome(int items, int itemsDiff, Function<Integer, Double> pItemWin) {
		if (itemsDiff > 2)
			throw new IllegalArgumentException();
		this.items = items;
		this.itemsDiff = itemsDiff;
		this.pItemWin = pItemWin;
	}

	public double pWin() {
		return pWin(0, 0);
	}

	public double pWin(int items1, int items2) {
		if (items1 == items) {
			int diff = items1 - items2;
			if (diff >= itemsDiff)
				return 1.0;
			else {
				int nextItem = items1 + items2 + 1;
				switch (diff) {
					case 0: return pDeuce(pItemWin.apply(nextItem), pItemWin.apply(nextItem + 1), items1, items2);
					case 1: {
						double p = pItemWin.apply(nextItem);
						return p + (1 - p) * pDeuce(pItemWin.apply(nextItem + 1), pItemWin.apply(nextItem + 2), items1, items2);
					}
					default: throw new IllegalStateException();
				}
			}
		}
		if (items2 >= items) {
			int diff = items2 - items1;
			if (diff >= itemsDiff)
				return 0.0;
			else {
				int nextItem = items1 + items2 + 1;
				switch (diff) {
					case 0: return pDeuce(pItemWin.apply(nextItem), pItemWin.apply(nextItem + 1), items1, items2);
					case 1: {
						double p = pItemWin.apply(nextItem);
						return p * pDeuce(pItemWin.apply(nextItem + 1), pItemWin.apply(nextItem + 2), items1, items2);
					}
					default: throw new IllegalStateException();
				}
			}
		}
		double p = pItemWin.apply(items1 + items2 + 1);
		return p * pWin(items1 + 1, items2) + (1 - p) * pWin(items1, items2 + 1);
	}

	protected double pDeuce(double p1, double p2, int items1, int items2) {
		double p12 = p1 * p2;
		return p12 / (1.0 - p1 - p2 + 2 * p12);
	}
}
