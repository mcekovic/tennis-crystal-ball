package org.strangeforest.tcb.model;

import java.util.function.*;

public abstract class DiffOutcome {

	private final int maxItems;
	private final int itemsDiff;
	private final Function<Integer, Double> pItemWin;

	protected DiffOutcome(int maxItems, int itemsDiff, Function<Integer, Double> pItemWin) {
		this.maxItems = maxItems;
		this.itemsDiff = itemsDiff;
		this.pItemWin = pItemWin;
	}

	public double pWin() {
		return pWin(0, 0);
	}

	double pWin(int items1, int items2) {
		if (items1 == maxItems) {
			int diff = items1 - items2;
			if (diff >= itemsDiff)
				return 1.0;
			else {
				int nextItem = items1 + items2 + 1;
				if (diff == 0)
					return pDeuce(pItemWin.apply(nextItem), pItemWin.apply(nextItem + 1));
				else if (diff == 1) {
					double p = pItemWin.apply(nextItem);
					return p + (1 - p) * pDeuce(pItemWin.apply(nextItem + 1), pItemWin.apply(nextItem + 2));
				}
				else
					throw new IllegalStateException();
			}
		}
		if (items2 >= maxItems) {
			int diff = items2 - items1;
			if (diff >= itemsDiff)
				return 0.0;
			else {
				int nextItem = items1 + items2 + 1;
				if (diff == 0)
					return pDeuce(pItemWin.apply(nextItem), pItemWin.apply(nextItem + 1));
				else if (diff == 1) {
					double p = pItemWin.apply(nextItem);
					return p * pDeuce(pItemWin.apply(nextItem + 1), pItemWin.apply(nextItem + 2));
				}
				else
					throw new IllegalStateException();
			}
		}
		double p = pItemWin.apply(items1 + items2 + 1);
		return p * pWin(items1 + 1, items2) + (1 - p) * pWin(items1, items2 + 1);
	}

	protected double pDeuce(double p1, double p2) {
		double p12 = p1 * p2;
		return p12 / (1 - p1 - p2 + 2 * p12);
	}
}
