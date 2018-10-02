package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.strangeforest.tcb.stats.model.PlayerTournamentTimeline.*;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

class PlayerTimelineTest {

	@Test
	void simplePlayerTimelineIsCreated() {
		PlayerTimeline timeline = new PlayerTimeline(emptySet());
		timeline.addItem(new PlayerTimelineItem(1, 1, "Australian Open", 2001, 101, LocalDate.of(2001, 1, 15), "G", "H", false, "Australian Open", "SF"));
		timeline.addItem(new PlayerTimelineItem(1, 1, "Australian Open", 2002, 201, LocalDate.of(2002, 1, 20), "G", "H", false, "Australian Open", "QF"));
		timeline.addItem(new PlayerTimelineItem(1, 1, "Australian Open", 2003, 301, LocalDate.of(2003, 1, 18), "G", "H", false, "Australian Open", "W"));

		timeline.addItem(new PlayerTimelineItem(3, 3, "Miami", 2002, 203, LocalDate.of(2002, 3, 5), "M", "H", false, "Miami", "QF"));
		timeline.addItem(new PlayerTimelineItem(3, 3, "Miami", 2003, 303, LocalDate.of(2003, 3, 8), "M", "H", false, "Miami", "F"));

		timeline.addItem(new PlayerTimelineItem(4, 4, "Hamburg Masters", 2001, 104, LocalDate.of(2001, 4, 20), "M", "C", false, "Hamburg Masters", "R16"));
		timeline.addItem(new PlayerTimelineItem(4, 4, "Hamburg Masters", 2008, 804, LocalDate.of(2008, 4, 25), "A", "C", false, "Hamburg", "QF"));

		timeline.addItem(new PlayerTimelineItem(2, 2, "Roland Garros", 2002, 202, LocalDate.of(2002, 5, 10), "G", "C", false, "Roland Garros", "R16"));
		timeline.addItem(new PlayerTimelineItem(2, 2, "Roland Garros", 2003, 302, LocalDate.of(2003, 5, 18), "G", "C", false, "Roland Garros", "R32"));
		timeline.addItem(new PlayerTimelineItem(2, 2, "Roland Garros", 2004, 402, LocalDate.of(2004, 5, 15), "G", "C", false, "Roland Garros", "R64"));

		timeline.addItem(new PlayerTimelineItem(5, 5, "Stuttgart", 2001, 104, LocalDate.of(2001, 7, 20), "M", "C", false, "Stuttgart Masters", "R32"));
		timeline.addItem(new PlayerTimelineItem(5, 5, "Stuttgart", 2009, 904, LocalDate.of(2009, 7, 25), "A", "G", false, "Stuttgart", "SF"));


		assertThat(timeline.getSeasons()).containsExactly(2001, 2002, 2003, 2004, 2008, 2009);
		List<PlayerTournamentTimeline> tournaments = timeline.getBigTournaments();
		assertThat(tournaments).hasSize(5);

		PlayerTournamentTimeline tournament1 = tournaments.get(0);
		assertThat(tournament1.getMaxLevel()).isEqualTo("G");
		assertThat(tournament1.getLevelsAndNames()).containsExactly(entry("G", "Australian Open"));
		assertThat(tournament1.isFirstByLevel()).isTrue();
		assertThat(tournament1.getSurfaces()).containsExactly(new TimelineSurface("H"));
		assertThatItemSeasonsAre(tournament1.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament2 = tournaments.get(1);
		assertThat(tournament2.getMaxLevel()).isEqualTo("G");
		assertThat(tournament2.getLevelsAndNames()).containsExactly(entry("G", "Roland Garros"));
		assertThat(tournament2.isFirstByLevel()).isFalse();
		assertThat(tournament2.getSurfaces()).containsExactly(new TimelineSurface("C"));
		assertThatItemSeasonsAre(tournament2.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament3 = tournaments.get(2);
		assertThat(tournament3.getMaxLevel()).isEqualTo("M");
		assertThat(tournament3.getLevelsAndNames()).containsExactly(entry("M", "Miami"));
		assertThat(tournament3.isFirstByLevel()).isTrue();
		assertThat(tournament3.getSurfaces()).containsExactly(new TimelineSurface("H"));
		assertThatItemSeasonsAre(tournament3.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament4 = tournaments.get(3);
		assertThat(tournament4.getMaxLevel()).isEqualTo("M");
		assertThat(tournament4.getLevelsAndNames()).containsExactly(entry("M", "Hamburg Masters"), entry("A", "Hamburg"));
		assertThat(tournament4.isFirstByLevel()).isFalse();
		assertThat(tournament4.getSurfaces()).containsExactly(new TimelineSurface("C"));
		assertThatItemSeasonsAre(tournament4.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament5 = tournaments.get(4);
		assertThat(tournament5.getMaxLevel()).isEqualTo("M");
		assertThat(tournament5.getLevelsAndNames()).containsExactly(entry("M", "Stuttgart Masters"), entry("A", "Stuttgart"));
		assertThat(tournament5.isFirstByLevel()).isFalse();
		assertThat(tournament5.getSurfaces()).containsExactly(new TimelineSurface("C"), new TimelineSurface("G"));
		assertThatItemSeasonsAre(tournament5.getItems(), timeline.getSeasons());
	}

	private void assertThatItemSeasonsAre(List<PlayerTimelineItem> items, Collection<Integer> seasons) {
		assertThat(items).hasSize(seasons.size());
		Iterator<Integer> iter = seasons.iterator();
		for (PlayerTimelineItem item : items)
			assertThat(item.getSeason()).isEqualTo(iter.next());
	}
}
