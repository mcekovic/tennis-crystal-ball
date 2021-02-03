package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.model.core.*;

import static java.lang.String.*;

public class MentalToughnessRow extends PlayerRow {

	private final String rating;
	private final WonLost points;
	private final WonLost decidingSets;
	private final WonLost fifthSets;
	private final WonLost finals;
	private final WonLost tieBreaks;
	private final WonLost decidingSetTieBreaks;

	public MentalToughnessRow(int rank, int playerId, String name, String countryId, Boolean active, WonLost points, WonLost decidingSets, WonLost fifthSets, WonLost finals, WonLost tieBreaks, WonLost decidingSetTieBreaks) {
		super(rank, playerId, name, countryId, active);
		rating = points.getLost() > 0 ? format("%.3f", ((double)points.getWon()) / points.getLost()) : "<i class=\"fa fa-heartbeat\"></i>";
		this.points = points;
		this.decidingSets = decidingSets;
		this.fifthSets = fifthSets;
		this.finals = finals;
		this.tieBreaks = tieBreaks;
		this.decidingSetTieBreaks = decidingSetTieBreaks;
	}

	public String getRating() {
		return rating;
	}

	public int getPointsWon() {
		return points.getWon();
	}

	public int getPointsLost() {
		return points.getLost();
	}

	public String getDecidingSetsPct() {
		return decidingSets.getWonPctStr();
	}

	public String getDecidingSets() {
		return decidingSets.getWL();
	}

	public String getFifthSetsPct() {
		return fifthSets.getWonPctStr();
	}

	public String getFifthSets() {
		return fifthSets.getWL();
	}

	public String getFinalsPct() {
		return finals.getWonPctStr();
	}

	public String getFinals() {
		return finals.getWL();
	}

	public String getTieBreaksPct() {
		return tieBreaks.getWonPctStr();
	}

	public String getTieBreaks() {
		return tieBreaks.getWL();
	}

	public String getDecidingSetTieBreaksPct() {
		return decidingSetTieBreaks.getWonPctStr();
	}

	public String getDecidingSetTieBreaks() {
		return decidingSetTieBreaks.getWL();
	}
}
