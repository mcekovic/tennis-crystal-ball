package org.strangeforest.tcb.stats.model.learning;

public class VectorCost<V, C extends Comparable<C>> {

	public final V vector;
	public final C cost;

	public VectorCost(V vector, C cost) {
		this.vector = vector;
		this.cost = cost;
	}
}
