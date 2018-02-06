package org.strangeforest.tcb.stats.model.prediction;

import static org.strangeforest.tcb.stats.model.prediction.TuningSetLevel.*;

public enum TuningSet {

	ALL("", "", TOP),
	HARD_OUTDOOR(" AND surface = 'H' AND NOT indoor", "-hard-outdoor", SURFACE),
	CLAY(" AND surface = 'C'", "-clay", SURFACE),
	GRASS(" AND surface = 'G'", "-grass", SURFACE),
	HARD_INDOOR_CARPET(" AND ((surface = 'H' AND indoor) OR surface = 'P')", "-hard-indoor-carpet", SURFACE),
	BEST_OF_3(" AND best_of = 3", "-best-of-3", BEST_OF),
	BEST_OF_5(" AND best_of = 5", "-best-of-5", BEST_OF),
	HARD_OUTDOOR_BEST_OF_3(" AND surface = 'H' AND NOT indoor AND best_of = 3", "-hard-outdoor-best-of-3", SURFACE_AND_BEST_OF),
	HARD_OUTDOOR_BEST_OF_5(" AND surface = 'H' AND NOT indoor AND best_of = 5", "-hard-outdoor-best-of-5", SURFACE_AND_BEST_OF),
	CLAY_BEST_OF_3(" AND surface = 'C' AND best_of = 3", "-clay-best-of-3", SURFACE_AND_BEST_OF),
	CLAY_BEST_OF_5(" AND surface = 'C' AND best_of = 5", "-clay-best-of-5", SURFACE_AND_BEST_OF),
	GRASS_BEST_OF_3(" AND surface = 'G' AND best_of = 3", "-grass-best-of-3", SURFACE_AND_BEST_OF),
	GRASS_BEST_OF_5(" AND surface = 'G' AND best_of = 5", "-grass-best-of-5", SURFACE_AND_BEST_OF);

	private final String condition;
	private final String configSuffix;
	private final TuningSetLevel level;

	TuningSet(String condition, String configSuffix, TuningSetLevel level) {
		this.condition = condition;
		this.configSuffix = configSuffix;
		this.level = level;
	}

	public String getCondition() {
		return condition;
	}

	public String getConfigSuffix() {
		return configSuffix;
	}

	public TuningSetLevel getLevel() {
		return level;
	}
}
