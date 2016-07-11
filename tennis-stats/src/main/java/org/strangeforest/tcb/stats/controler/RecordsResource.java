package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;

@RestController
public class RecordsResource {

	@Autowired private RecordsService recordsService;

	@RequestMapping("/recordTable")
	public BootgridTable<RecordRow> recordTable(
		@RequestParam(value = "recordId") String recordId,
		@RequestParam(value = "active") boolean active,
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount
	) {
		int pageSize = rowCount > 0 ? rowCount : RecordsService.MAX_PLAYER_COUNT;
		return recordsService.getRecordTable(recordId, active, pageSize, current);
	}
}
