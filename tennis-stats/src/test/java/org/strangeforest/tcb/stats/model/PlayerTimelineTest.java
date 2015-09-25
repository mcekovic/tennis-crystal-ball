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
		timeline.addItem(new PlayerTimelineItem(1, 2001, 101, toDate(LocalDate.of(2001, 1, 15)), "G", "H", "Australian Open", "SF"));
		timeline.addItem(new PlayerTimelineItem(1, 2002, 201, toDate(LocalDate.of(2002, 1, 20)), "G", "H", "Australian Open", "QF"));
		timeline.addItem(new PlayerTimelineItem(1, 2003, 301, toDate(LocalDate.of(2003, 1, 18)), "G", "H", "Australian Open", "W"));

		timeline.addItem(new PlayerTimelineItem(3, 2002, 203, toDate(LocalDate.of(2002, 3, 5)), "M", "H", "Miami", "QF"));
		timeline.addItem(new PlayerTimelineItem(3, 2003, 303, toDate(LocalDate.of(2003, 3, 8)), "M", "H", "Miami", "F"));

		timeline.addItem(new PlayerTimelineItem(4, 2001, 104, toDate(LocalDate.of(2001, 4, 20)), "M", "C", "Monte Carlo", "R16"));
		timeline.addItem(new PlayerTimelineItem(4, 2004, 404, toDate(LocalDate.of(2004, 4, 25)), "M", "C", "Monte Carlo", "QF"));

		timeline.addItem(new PlayerTimelineItem(2, 2002, 202, toDate(LocalDate.of(2002, 5, 10)), "G", "C", "Roland Garros", "R16"));
		timeline.addItem(new PlayerTimelineItem(2, 2003, 302, toDate(LocalDate.of(2003, 5, 18)), "G", "C", "Roland Garros", "R32"));
		timeline.addItem(new PlayerTimelineItem(2, 2004, 402, toDate(LocalDate.of(2004, 5, 15)), "G", "C", "Roland Garros", "R64"));


		assertThat(timeline.getSeasons()).containsExactly(2001, 2002, 2003, 2004);
   	assertThat(timeline.getTournaments()).hasSize(4);

		PlayerTournamentTimeline tournament1 = timeline.getTournaments().get(0);
		assertThat(tournament1.getName()).isEqualTo("Australian Open");
		assertThat(tournament1.isFirstByLevel()).isTrue();
		assertThatItemSeasonsAre(tournament1.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament2 = timeline.getTournaments().get(1);
		assertThat(tournament2.getName()).isEqualTo("Roland Garros");
		assertThat(tournament2.isFirstByLevel()).isFalse();
		assertThatItemSeasonsAre(tournament2.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament3 = timeline.getTournaments().get(2);
		assertThat(tournament3.getName()).isEqualTo("Miami");
		assertThat(tournament3.isFirstByLevel()).isTrue();
		assertThatItemSeasonsAre(tournament3.getItems(), timeline.getSeasons());

		PlayerTournamentTimeline tournament4 = timeline.getTournaments().get(3);
		assertThat(tournament4.getName()).isEqualTo("Monte Carlo");
		assertThat(tournament4.isFirstByLevel()).isFalse();
		assertThatItemSeasonsAre(tournament4.getItems(), timeline.getSeasons());
	}

	private void assertThatItemSeasonsAre(List<PlayerTimelineItem> items, Collection<Integer> seasons) {
		assertThat(items).hasSize(seasons.size());
		Iterator<Integer> iter = seasons.iterator();
		for (PlayerTimelineItem item : items)
			assertThat(item.getSeason()).isEqualTo(iter.next());
	}
}
