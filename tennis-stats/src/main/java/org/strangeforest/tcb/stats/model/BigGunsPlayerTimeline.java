package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.stream.Collectors.*;

public class BigGunsPlayerTimeline extends PlayerRow {

	private final String lastName;
	private final Date dob;
	private final int goatPoints;
	private final Map<Integer, SeasonPoints> seasons = new HashMap<>(); // <Season, SeasonPoints>
	private BigGunsTimeline timeline;

	public BigGunsPlayerTimeline(int rank, int playerId, String name, String lastName, String countryId, boolean active, Date dob, int goatPoints) {
		super(rank, playerId, name, countryId, active);
		this.lastName = lastName;
		this.dob = dob;
		this.goatPoints = goatPoints;
	}

	public String getLastName() {
		return lastName;
	}

	public Date getDob() {
		return dob;
	}

	public int getGoatPoints() {
		return goatPoints;
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

	public SeasonPoints getSeasonPoints(int season) {
		return seasons.get(season);
	}

	public void addSeasonPoints(SeasonPoints seasonPoints) {
		seasons.put(seasonPoints.getSeason(), seasonPoints);
	}

	void setTimeline(BigGunsTimeline timeline) {
		this.timeline = timeline;
	}
}
