package org.strangeforest.tcb.stats.model.records;

import org.strangeforest.tcb.stats.model.records.rows.*;

public enum RecordRowFactory {

	INTEGER {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new IntegerRecordRow(rank, playerId, name, countryId, active);
		}
	},
	SEASON_INTEGER {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new SeasonIntegerRecordRow(rank, playerId, name, countryId, active);
		}
	},
	SEASON_RANGE_INTEGER {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new SeasonRangeIntegerRecordRow(rank, playerId, name, countryId, active);
		}
	},
	SEASON_TWO_INTEGERS {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new SeasonTwoIntegersRecordRow(rank, playerId, name, countryId, active);
		}
	},
	TOURNAMENT_INTEGER {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new TournamentIntegerRecordRow(rank, playerId, name, countryId, active);
		}
	},
	TOURNAMENT_EVENT_INTEGER {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new TournamentEventIntegerRecordRow(rank, playerId, name, countryId, active);
		}
	},
	DATE_INTEGER {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new DateIntegerRecordRow(rank, playerId, name, countryId, active);
		}
	},
	DATE_RANGE_INTEGER {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new DateRangeIntegerRecordRow(rank, playerId, name, countryId, active);
		}
	},
	DATE_AGE {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new DateAgeRecordRow(rank, playerId, name, countryId, active);
		}
	},
	STREAK {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new StreakRecordRow(rank, playerId, name, countryId, active);
		}
	},
	CAREER_SPAN {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new CareerSpanRecordRow(rank, playerId, name, countryId, active);
		}
	},
	TOURNAMENT_CAREER_SPAN {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new TournamentCareerSpanRecordRow(rank, playerId, name, countryId, active);
		}
	},
	TOURNAMENT_EVENT_AGE {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new TournamentEventAgeRecordRow(rank, playerId, name, countryId, active);
		}
	},
	RANKING_DIFF {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new RankingDiffRecordRow(rank, playerId, name, countryId, active);
		}
	},
	WINNING_PCT {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new WinningPctRecordRow(rank, playerId, name, countryId, active);
		}
	},
	LOSING_PCT {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new LosingPctRecordRow(rank, playerId, name, countryId, active);
		}
	},
	WINNING_W_DRAW_PCT {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new WinningWDrawPctRecordRow(rank, playerId, name, countryId, active);
		}
	},
	LOSING_W_DRAW_PCT {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new LosingWDrawPctRecordRow(rank, playerId, name, countryId, active);
		}
	},
	SEASON_WINNING_PCT {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new SeasonWinningPctRecordRow(rank, playerId, name, countryId, active);
		}
	},
	SEASON_LOSING_PCT {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new SeasonLosingPctRecordRow(rank, playerId, name, countryId, active);
		}
	},
	TOURNAMENT_WINNING_PCT {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new TournamentWinningPctRecordRow(rank, playerId, name, countryId, active);
		}
	},
	TOURNAMENT_LOSING_PCT {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
			return new TournamentLosingPctRecordRow(rank, playerId, name, countryId, active);
		}
	};

	public abstract RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active);
}
