package org.strangeforest.tcb.stats.model.prediction;

public interface PredictionItem extends Weighted {

	PredictionArea getArea();
	void setArea(PredictionArea area);
	boolean isForSet();
}
