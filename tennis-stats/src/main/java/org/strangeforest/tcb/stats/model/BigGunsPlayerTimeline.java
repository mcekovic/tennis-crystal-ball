package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.stream.Collectors.*;

public class BigGunsPlayerTimeline extends PlayerRow {

	private final Date dob;
	private final Map<Integer, SeasonPoints> seasons = new HashMap<>(); // <Season, SeasonPoints>
	private BigGunsTimeline timeline;

	public BigGunsPlayerTimeline(int rank, int playerId, String player, String countryId, Date dob) {
		super(rank, playerId, player, countryId);
		this.dob = dob;
	}

	public Date getDob() {
		return dob;
	}

	public Set<Integer> getSeasons() {
		return seasons.keySet();
	}

	public List<SeasonPoints> getSeasonsPoints() {
		return timeline.getSeasons().stream().map(season -> {
			SeasonPoints seasonPoints = seasons.get(season);
			return seasonPoints != null ? seasonPoints : new SeasonPoints(season, 0);
		}).collect(toList());
	}

	public void addSeasonPoints(SeasonPoints seasonPoints) {
		seasons.put(seasonPoints.getSeason(), seasonPoints);
	}

	void setTimeline(BigGunsTimeline timeline) {
		this.timeline = timeline;
	}
}
