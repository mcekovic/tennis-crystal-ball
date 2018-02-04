package org.strangeforest.tcb.stats.model.prediction;

public interface PredictionItem extends Weighted {

	String name();
	PredictionArea getArea();
	void setArea(PredictionArea area);
	boolean isForSet();
}
