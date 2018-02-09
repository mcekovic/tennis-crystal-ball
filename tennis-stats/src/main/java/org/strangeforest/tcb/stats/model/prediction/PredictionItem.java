package org.strangeforest.tcb.stats.model.prediction;

public interface PredictionItem extends Weighted {

	String name();
	String longName();
	PredictionArea getArea();
	void setArea(PredictionArea area);
	boolean isForSet();
}
