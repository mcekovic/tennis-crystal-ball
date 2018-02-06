package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;
import static org.strangeforest.tcb.stats.model.prediction.TuningSet.*;
import static org.strangeforest.tcb.util.EnumUtil.*;

public enum TuningSetLevel {
	
	TOP {
		@Override public TuningSet select(Surface surface, Boolean indoor, TournamentLevel level, Short bestOf) {
			return TuningSet.ALL;
		}
	},
	SURFACE {
		@Override public TuningSet select(Surface surface, Boolean indoor, TournamentLevel level, Short bestOf) {
			if (surface == null)
				return TuningSet.ALL;
			switch (surface) {
				case HARD: return indoor ? HARD_INDOOR_CARPET : HARD_OUTDOOR;
				case CLAY: return CLAY;
				case GRASS: return GRASS;
				case CARPET: return HARD_INDOOR_CARPET;
				default: throw unknownEnum(surface);
			}
		}
	},
	BEST_OF {
		@Override public TuningSet select(Surface surface, Boolean indoor, TournamentLevel level, Short bestOf) {
			bestOf = bestOf(level, bestOf);
			return bestOf == null ? ALL : (bestOf == 3 ? BEST_OF_3 : BEST_OF_5);
		}
	},
	SURFACE_AND_BEST_OF {
		@Override public TuningSet select(Surface surface, Boolean indoor, TournamentLevel level, Short bestOf) {
			if (surface == null)
				return BEST_OF.select(null, indoor, level, bestOf);
			switch (surface) {
				case HARD: return indoor ? HARD_INDOOR_CARPET : (defaultBestOf(level, bestOf) == 3 ? HARD_OUTDOOR_BEST_OF_3 : HARD_OUTDOOR_BEST_OF_5);
				case CLAY: return defaultBestOf(level, bestOf) == 3 ? CLAY_BEST_OF_3 : CLAY_BEST_OF_5;
				case GRASS: return defaultBestOf(level, bestOf) == 3 ? GRASS_BEST_OF_3 : GRASS_BEST_OF_5;
				case CARPET: return HARD_INDOOR_CARPET;
				default: throw unknownEnum(surface);
			}
		}
	};

	public abstract TuningSet select(Surface surface, Boolean indoor, TournamentLevel level, Short bestOf);
}
