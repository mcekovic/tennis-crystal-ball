package org.strangeforest.tcb.stats.model.prediction;

public abstract class DiffOutcome {

	private int items;
	private int itemsDiff;

	protected DiffOutcome(int items, int itemsDiff) {
		if (itemsDiff > 2)
			throw new IllegalArgumentException();
		this.items = items;
		this.itemsDiff = itemsDiff;
	}

	public double pWin() {
		return pWin(0, 0);
	}

	public double pWin(int items1, int items2) {
		int nextItem = items1 + items2 + 1;
		if (items1 >= items) {
			int diff = items1 - items2;
			if (diff >= itemsDiff)
				return 1.0;
			switch (diff) {
				case 0: return pDeuce(pItemWin(nextItem), pItemWin(nextItem + 1), items1, items2);
				case 1: {
					double p = pItemWin(nextItem);
					return p + (1.0 - p) * pDeuce(pItemWin(nextItem + 1), pItemWin(nextItem + 2), items1, items2 + 1);
				}
				default: throw new IllegalStateException();
			}
		}
		if (items2 >= items) {
			int diff = items2 - items1;
			if (diff >= itemsDiff)
				return 0.0;
			switch (diff) {
				case 0: return pDeuce(pItemWin(nextItem), pItemWin(nextItem + 1), items1, items2);
				case 1: {
					double p = pItemWin(nextItem);
					return p * pDeuce(pItemWin(nextItem + 1), pItemWin(nextItem + 2), items1 + 1, items2);
				}
				default: throw new IllegalStateException();
			}
		}
		double p = pItemWin(nextItem);
		return p * pWin(items1 + 1, items2) + (1.0 - p) * pWin(items1, items2 + 1);
	}

	protected abstract double pItemWin(int item);

	protected double pDeuce(double p1, double p2, int items1, int items2) {
		double p12 = p1 * p2;
		return p12 / (1.0 - p1 - p2 + 2.0 * p12);
	}
}
