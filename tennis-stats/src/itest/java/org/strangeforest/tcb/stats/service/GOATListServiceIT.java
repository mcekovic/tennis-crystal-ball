package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

@ServiceTest
class GOATListServiceIT {

	@Autowired private GOATListService goatListService;

	@Test
	void goatTopN() {
		var goatTopN = goatListService.getGOATTopN(10);

		assertThat(goatTopN).hasSize(10);
	}

	@Test
	void goatList() {
		var goatList = goatListService.getGOATListTable(1000, null, new PlayerListFilter(""), GOATListConfig.DEFAULT, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	void hardGoatList() {
		var goatList = goatListService.getGOATListTable(1000, "H", new PlayerListFilter(""), GOATListConfig.DEFAULT, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	void clayGoatList() {
		var goatList = goatListService.getGOATListTable(1000, "C", new PlayerListFilter(""), GOATListConfig.DEFAULT, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	void grassGoatList() {
		var goatList = goatListService.getGOATListTable(1000, "G", new PlayerListFilter(""), GOATListConfig.DEFAULT, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	void carpetGoatList() {
		var goatList = goatListService.getGOATListTable(1000, "P", new PlayerListFilter(""), GOATListConfig.DEFAULT, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	void goatListTournamentX2() {
		var goatList = goatListService.getGOATListTable(1000, null, new PlayerListFilter(""), new GOATListConfig(true, false, 2, 1, 1, emptyMap(), emptyMap(), 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	void goatListGrandSlamTournamentX2() {
		var goatList = goatListService.getGOATListTable(1000, null, new PlayerListFilter(""), new GOATListConfig(true, false, 1, 1, 1, Map.of("G", 2), emptyMap(), 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	void goatListWeeksAtNo1X2() {
		var goatList = goatListService.getGOATListTable(1000, null, new PlayerListFilter(""), new GOATListConfig(true, false, 1, 1, 1, emptyMap(), emptyMap(), 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}

	@Test
	void goatListWeeksGSAndNo1FocusExtrapolate() {
		var goatList = goatListService.getGOATListTable(1000, null, new PlayerListFilter(""), new GOATListConfig(true, true, 1, 1, 1, Map.of("G", 2), Map.of("W", 2), 1, 1, 4, 1, 1, 4, 1, 1, 1, 1, 1, 1, 1), "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}
}
