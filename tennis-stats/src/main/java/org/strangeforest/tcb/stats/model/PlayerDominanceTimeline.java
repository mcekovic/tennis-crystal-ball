package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;

import com.google.common.collect.*;

import static java.util.stream.Collectors.*;

public class PlayerDominanceTimeline extends PlayerRow {

	private final String lastName;
	private final LocalDate dob;
	private final Surface surface;
	private final int goatPoints;
	private final Map<Integer, SeasonPoints> seasons = new HashMap<>(); // <Season, SeasonPoints>
	private DominanceTimeline timeline;

	public PlayerDominanceTimeline(int rank, int playerId, String name, String lastName, String countryId, boolean active, LocalDate dob, Surface surface, int goatPoints) {
		super(rank, playerId, name, countryId, active);
		this.lastName = lastName;
		this.dob = dob;
		this.surface = surface;
		this.goatPoints = goatPoints;
	}

	public PlayerDominanceTimeline(PlayerDominanceTimeline timeline) {
		super(timeline);
		lastName = timeline.lastName;
		dob = timeline.dob;
		surface = timeline.surface;
		goatPoints = timeline.goatPoints;
	}

	public String getLastName() {
		return lastName;
	}

	public LocalDate getDob() {
		return dob;
	}

	public int getGoatPoints() {
		return goatPoints;
	}

	public Set<Integer> getSeasons() {
		return seasons.keySet();
	}

	public boolean hasSeasons() {
		return !seasons.isEmpty();
	}

	public List<SeasonPoints> getSeasonsPoints() {
		return timeline.getSeasons().stream().map(season -> {
			var seasonPoints = seasons.get(season);
			return seasonPoints != null ? seasonPoints : new SeasonPoints(season, surface, 0);
		}).collect(toList());
	}

	public SeasonPoints getSeasonPoints(int season) {
		return seasons.get(season);
	}

	public void addSeasonPoints(SeasonPoints seasonPoints) {
		seasons.put(seasonPoints.getSeason(), seasonPoints);
	}

	void setTimeline(DominanceTimeline timeline) {
		this.timeline = timeline;
	}

	public PlayerDominanceTimeline filterSeasons(Range<Integer> seasonRange) {
		var player = new PlayerDominanceTimeline(this);
		for (var seasonPoints : seasons.values()) {
			if (seasonRange.contains(seasonPoints.getSeason()))
				player.addSeasonPoints(seasonPoints);
		}
		return player;
	}
}
