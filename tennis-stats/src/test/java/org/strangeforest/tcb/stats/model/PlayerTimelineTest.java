package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;

import org.junit.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.tcb.stats.util.DateUtil.*;

public class PlayerTimelineTest {

	@Test
	public void simplePlayerTimelineIsCreated() {
		PlayerTimeline timeline = new PlayerTimeline();
		timeline.addItem(new PlayerTimelineItem(1, "Australian Open", 2001, 101, toDate(LocalDate.of(2001, 1, 15)), "G", "H", "Australian Open", "SF"));
		timeline.addItem(new PlayerTimelineItem(1, "Australian Open", 2002, 201, toDate(LocalDate.of(2002, 1, 20)), "G", "H", "Australian Open", "QF"));
		timeline.addItem(new PlayerTimelineItem(1, "Australian Open", 2003, 301, toDate(LocalDate.of(2003, 1, 18)), "G", "H", "Australian Open", "W"));

		timeline.addItem(new PlayerTimelineItem(3, "Miami", 2002, 203, toDate(LocalDate.of(2002, 3, 5)), "M", "H", "Miami", "QF"));
		timeline.addItem(new PlayerTimelineItem(3, "Miami", 2003, 303, toDate(LocalDate.of(2003, 3, 8)), "M", "H", "Miami", "F"));

		timeline.addItem(new PlayerTimelineItem(4, "Hamburg Masters", 2001, 104, toDate(LocalDate.of(2001, 4, 20)), "M", "C", "Hamburg Masters", "R16"));
		timeline.addItem(new PlayerTimelineItem(4, "Hamburg Masters", 2008, 804, toDate(LocalDate.of(2008, 4, 25)), "A", "C", "Hamburg", "QF"));

		timeline.addItem(new PlayerTimelineItem(2, "Roland Garros", 2002, 202, toDate(LocalDate.of(2002, 5, 10)), "G", "C", "Roland Garros", "R16"));
		timeline.addItem(new PlayerTimelineItem(2, "Roland Garros", 2003, 302, toDate(LocalDate.of(2003, 5, 18)), "G", "C", "Roland Garros", "R32"));
		timeline.addItem(new PlayerTimelineItem(2, "Roland Garros", 2004, 402, toDate(LocalDate.of(2004, 5, 15)), "G", "C", "Roland Garros", "R64"));

		timeline.addItem(new PlayerTimelineItem(5, "Stuttgart", 2001, 104, toDate(LocalDate.of(2001, 7, 20)), "M", "C", "Stuttgart Masters", "R32"));
		timeline.addItem(new PlayerTimelineItem(5, "Stuttgart", 2009, 904, toDate(LocalDate.of(2009, 7, 25)), "A", "G", "Stuttgart", "SF"));


		assertThat(timeline.getSeasons()).containsExactly(2001, 2002, 2003, 2004, 2008, 2009);
		List<PlayerTournamentTimeline> tournaments = timeline.getTournaments();
		assertThat(tournaments).hasSize(5);

		PlayerTournamentTimeline tournament1 = tournaments.get(0);
		assertThat(tournament1.getMaxLevel()).isEqualTo("G");
		assertThat(tournament1.getLevelsAndNames()).containsExactly(entry("G", "Australian Open"));
		assertThat(tournament1.isFirstByLevel()).isTrue();
		assertThat(tournament1.getSurfaces()).containsExactly("H");
		assertThatItemSeasonsAre(tournament1.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament2 = tournaments.get(1);
		assertThat(tournament2.getMaxLevel()).isEqualTo("G");
		assertThat(tournament2.getLevelsAndNames()).containsExactly(entry("G", "Roland Garros"));
		assertThat(tournament2.isFirstByLevel()).isFalse();
		assertThat(tournament2.getSurfaces()).containsExactly("C");
		assertThatItemSeasonsAre(tournament2.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament3 = tournaments.get(2);
		assertThat(tournament3.getMaxLevel()).isEqualTo("M");
		assertThat(tournament3.getLevelsAndNames()).containsExactly(entry("M", "Miami"));
		assertThat(tournament3.isFirstByLevel()).isTrue();
		assertThat(tournament3.getSurfaces()).containsExactly("H");
		assertThatItemSeasonsAre(tournament3.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament4 = tournaments.get(3);
		assertThat(tournament4.getMaxLevel()).isEqualTo("M");
		assertThat(tournament4.getLevelsAndNames()).containsExactly(entry("M", "Hamburg Masters"), entry("A", "Hamburg"));
		assertThat(tournament4.isFirstByLevel()).isFalse();
		assertThat(tournament4.getSurfaces()).containsExactly("C");
		assertThatItemSeasonsAre(tournament4.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament5 = tournaments.get(4);
		assertThat(tournament5.getMaxLevel()).isEqualTo("M");
		assertThat(tournament5.getLevelsAndNames()).containsExactly(entry("M", "Stuttgart Masters"), entry("A", "Stuttgart"));
		assertThat(tournament5.isFirstByLevel()).isFalse();
		assertThat(tournament5.getSurfaces()).containsExactly("C", "G");
		assertThatItemSeasonsAre(tournament5.getItems(), timeline.getSeasons());
	}

	private void assertThatItemSeasonsAre(List<PlayerTimelineItem> items, Collection<Integer> seasons) {
		assertThat(items).hasSize(seasons.size());
		Iterator<Integer> iter = seasons.iterator();
		for (PlayerTimelineItem item : items)
			assertThat(item.getSeason()).isEqualTo(iter.next());
	}
}
