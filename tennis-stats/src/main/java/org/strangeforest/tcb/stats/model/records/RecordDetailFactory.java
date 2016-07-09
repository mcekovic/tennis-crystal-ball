package org.strangeforest.tcb.stats.model.records;

import org.strangeforest.tcb.stats.model.records.rows.*;

import static java.lang.String.*;

public enum RecordDetailFactory {

	INTEGER(IntegerRecordDetail.class),
	SEASON_INTEGER(SeasonIntegerRecordDetail.class),
	SEASON_RANGE_INTEGER(SeasonRangeIntegerRecordDetail.class),
	SEASON_TWO_INTEGERS(SeasonTwoIntegersRecordDetail.class),
	TOURNAMENT_INTEGER(TournamentIntegerRecordDetail.class),
	TOURNAMENT_EVENT_INTEGER(TournamentEventIntegerRecordDetail.class),
	DATE_INTEGER(DateIntegerRecordDetail.class),
	DATE_RANGE_INTEGER(DateRangeIntegerRecordDetail.class),
	DATE_AGE(DateAgeRecordDetail.class),
	STREAK(StreakRecordDetail.class),
	CAREER_SPAN(CareerSpanRecordDetail.class),
	TOURNAMENT_CAREER_SPAN(TournamentCareerSpanRecordDetail.class),
	TOURNAMENT_EVENT_AGE(TournamentEventAgeRecordDetail.class),
	RANKING_DIFF(RankingDiffRecordDetail.class),
	WINNING_PCT(WinningPctRecordDetail.class),
	LOSING_PCT(LosingPctRecordDetail.class),
	WINNING_W_DRAW_PCT(WinningWDrawPctRecordDetail.class),
	LOSING_W_DRAW_PCT(LosingWDrawPctRecordDetail.class),
	SEASON_WINNING_PCT(SeasonWinningPctRecordDetail.class),
	SEASON_LOSING_PCT(SeasonLosingPctRecordDetail.class),
	TOURNAMENT_WINNING_PCT(TournamentWinningPctRecordDetail.class),
	TOURNAMENT_LOSING_PCT(TournamentLosingPctRecordDetail.class);

	private final Class<? extends RecordDetail> detailClass;

	RecordDetailFactory(Class<? extends RecordDetail> detailClass) {
		this.detailClass = detailClass;
	}

	public RecordDetail createDetail() {
		try {
			return detailClass.newInstance();
		}
		catch (Exception ex) {
			throw new IllegalStateException(format("RecordDetail %s cannot be instantiated.", detailClass.getName()), ex);
		}
	}
}
