package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit4.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import com.google.common.collect.*;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class GOATListServiceIT {

	@Autowired private GOATListService goatListService;

	@Test
	public void goatTopN() {
		List<PlayerRanking> goatTopN = goatListService.getGOATTopN(10);

		assertThat(goatTopN).hasSize(10);
	}

	@Test
	public void goatList() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, null, new PlayerListFilter(""), GOATListConfig.DEFAULT, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	public void hardGoatList() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, "H", new PlayerListFilter(""), GOATListConfig.DEFAULT, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	public void clayGoatList() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, "C", new PlayerListFilter(""), GOATListConfig.DEFAULT, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	public void grassGoatList() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, "G", new PlayerListFilter(""), GOATListConfig.DEFAULT, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	public void carpetGoatList() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, "P", new PlayerListFilter(""), GOATListConfig.DEFAULT, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	public void goatListTournamentX2() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, null, new PlayerListFilter(""), new GOATListConfig(true, false, 2, 1, 1, emptyMap(), emptyMap(), 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	public void goatListGrandSlamTournamentX2() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, null, new PlayerListFilter(""), new GOATListConfig(true, false, 1, 1, 1, ImmutableMap.of("G", 2), emptyMap(), 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	public void goatListWeeksAtNo1X2() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, null, new PlayerListFilter(""), new GOATListConfig(true, false, 1, 1, 1, emptyMap(), emptyMap(), 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	public void goatListWeeksGSAndNo1FocusExtrapolate() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, null, new PlayerListFilter(""), new GOATListConfig(true, true, 1, 1, 1, ImmutableMap.of("G", 2), ImmutableMap.of("W", 2), 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1), "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}
}
