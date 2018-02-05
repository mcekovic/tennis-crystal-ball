package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;
import static org.strangeforest.tcb.util.EnumUtil.*;

public enum TuningSet {

	ALL("", "", true),
	BEST_OF_3(" AND best_of = 3", "-best-of-3", true),
	BEST_OF_5(" AND best_of = 5", "-best-of-5", true),
	HARD_OUTDOOR_BEST_OF_3(" AND surface = 'H' AND NOT indoor AND best_of = 3", "-hard-outdoor-best-of-3", false),
	HARD_OUTDOOR_BEST_OF_5(" AND surface = 'H' AND NOT indoor AND best_of = 5", "-hard-outdoor-best-of-5", false),
	HARD_INDOOR_CARPET(" AND ((surface = 'H' AND indoor) OR surface = 'P')", "-hard-indoor-carpet", false),
	CLAY_BEST_OF_3(" AND surface = 'C' AND best_of = 3", "-clay-best-of-3", false),
	CLAY_BEST_OF_5(" AND surface = 'C' AND best_of = 5", "-clay-best-of-5", false),
	GRASS_BEST_OF_3(" AND surface = 'G' AND best_of = 3", "-grass-best-of-3", false),
	GRASS_BEST_OF_5(" AND surface = 'G' AND best_of = 5", "-grass-best-of-5", false);

	private final String condition;
	private final String configSuffix;
	private final boolean compound;

	TuningSet(String condition, String configSuffix, boolean compound) {
		this.condition = condition;
		this.configSuffix = configSuffix;
		this.compound = compound;
	}

	public String getCondition() {
		return condition;
	}

	public String getConfigSuffix() {
		return configSuffix;
	}

	public boolean isCompound() {
		return compound;
	}

	public static TuningSet select(Surface surface, Boolean indoor, TournamentLevel level, Short bestOf) {
		if (surface == null) {
			bestOf = bestOf(level, bestOf);
			return bestOf == null ? ALL : (bestOf == 3 ? BEST_OF_3 : BEST_OF_5);
		}
		switch (surface) {
			case HARD: return indoor ? HARD_INDOOR_CARPET : (defaultBestOf(level, bestOf) == 3 ? HARD_OUTDOOR_BEST_OF_3 : HARD_OUTDOOR_BEST_OF_5);
			case CLAY: return defaultBestOf(level, bestOf) == 3 ? CLAY_BEST_OF_3 : CLAY_BEST_OF_5;
			case GRASS: return defaultBestOf(level, bestOf) == 3 ? GRASS_BEST_OF_3 : GRASS_BEST_OF_5;
			case CARPET: return HARD_INDOOR_CARPET;
			default: throw unknownEnum(surface);
		}
	}
}
