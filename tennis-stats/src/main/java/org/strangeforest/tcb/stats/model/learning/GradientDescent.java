package org.strangeforest.tcb.stats.model.learning;

import java.util.*;
import java.util.function.*;

import static java.util.stream.Collectors.*;

public class GradientDescent<V, C extends Comparable<C>> {

	private final Function<V, C> cost;
	private final Function<V, List<V>> variations;
	private final BiFunction<VectorCost<V, C>, List<VectorCost<V, C>>, V> step;
	private final BiFunction<C, C, Boolean> completed;

	public GradientDescent(Function<V, C> cost, Function<V, List<V>> variations, BiFunction<VectorCost<V, C>, List<VectorCost<V, C>>, V> step, BiFunction<C, C, Boolean> completed) {
		this.cost = cost;
		this.variations = variations;
		this.step = step;
		this.completed = completed;
	}

	public VectorCost<V, C> minimizeCost(V startVector, int maxSteps) {
		var vectorCost = new VectorCost<V, C>(startVector, cost.apply(startVector));
		for (var i = 1; i <= maxSteps; i++) {
			var newVectorHost = doStep(vectorCost);
			if (completed.apply(newVectorHost.cost, vectorCost.cost))
				return newVectorHost;
			vectorCost = newVectorHost;
		}
		return vectorCost;
	}

	private VectorCost<V, C> doStep(VectorCost<V, C> vectorCost) {
		var variationsCosts = variations.apply(vectorCost.vector).stream().map(v -> new VectorCost<>(v, cost.apply(v))).collect(toList());
		var newVector = step.apply(vectorCost, variationsCosts);
		return new VectorCost<>(newVector, cost.apply(newVector));
	}
}
