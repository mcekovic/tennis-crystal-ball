package org.strangeforest.tcb.stats.service;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.junit4.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.table.*;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class GOATListServiceIT {

	@Autowired private GOATListService goatListService;

	@Test
	public void goatList() {
		BootgridTable<GOATListRow> goatList = goatListService.getGOATListTable(1000, new PlayerListFilter(""), false, false, "goat_points", 20, 1);

		assertThat(goatList.getRowCount()).isEqualTo(20);
		assertThat(goatList.getTotal()).isGreaterThanOrEqualTo(500);
	}
}
