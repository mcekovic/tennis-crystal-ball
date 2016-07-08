package org.strangeforest.tcb.stats.model.records;

import java.lang.reflect.*;

import org.strangeforest.tcb.stats.model.records.rows.*;

import static java.lang.String.*;

public enum RecordRowFactory {

	INTEGER(IntegerRecordRow.class),
	SEASON_INTEGER(SeasonIntegerRecordRow.class),
	SEASON_RANGE_INTEGER(SeasonRangeIntegerRecordRow.class),
	SEASON_TWO_INTEGERS(SeasonTwoIntegersRecordRow.class),
	TOURNAMENT_INTEGER(TournamentIntegerRecordRow.class),
	TOURNAMENT_EVENT_INTEGER(TournamentEventIntegerRecordRow.class),
	DATE_INTEGER(DateIntegerRecordRow.class),
	DATE_RANGE_INTEGER(DateRangeIntegerRecordRow.class),
	DATE_AGE(DateAgeRecordRow.class),
	STREAK(StreakRecordRow.class),
	CAREER_SPAN(CareerSpanRecordRow.class),
	TOURNAMENT_CAREER_SPAN(TournamentCareerSpanRecordRow.class),
	TOURNAMENT_EVENT_AGE(TournamentEventAgeRecordRow.class),
	RANKING_DIFF(RankingDiffRecordRow.class),
	WINNING_PCT(WinningPctRecordRow.class),
	LOSING_PCT(LosingPctRecordRow.class),
	WINNING_W_DRAW_PCT(WinningWDrawPctRecordRow.class),
	LOSING_W_DRAW_PCT(LosingWDrawPctRecordRow.class),
	SEASON_WINNING_PCT(SeasonWinningPctRecordRow.class),
	SEASON_LOSING_PCT(SeasonLosingPctRecordRow.class),
	TOURNAMENT_WINNING_PCT(TournamentWinningPctRecordRow.class),
	TOURNAMENT_LOSING_PCT(TournamentLosingPctRecordRow.class);

	private final Constructor<? extends RecordRow> constructor;

	RecordRowFactory(Class<? extends RecordRow> rowClass) {
		try {
			this.constructor = rowClass.getDeclaredConstructor(int.class, int.class, String.class, String.class, Boolean.class);
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException(format("RecordRow class %s does not have adequate constructor.", rowClass.getName()));
		}
	}

	public RecordRow createRow(int rank, int playerId, String name, String countryId, Boolean active) {
		try {
			return constructor.newInstance(rank, playerId, name, countryId, active);
		}
		catch (Exception ex) {
			throw new IllegalStateException(format("RecordRow %s cannot be instantiated.", constructor.getDeclaringClass().getName()), ex);
		}
	}
}
