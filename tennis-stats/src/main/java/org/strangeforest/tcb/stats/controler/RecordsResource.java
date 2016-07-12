package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;

@RestController
public class RecordsResource {
	@Autowired private RecordsService recordsService;

	private static final int MAX_RECORDS = 1000;
	private static final int MAX_PLAYERS =  500;

	@RequestMapping("/recordsTable")
	public BootgridTable<RecordRow> recordsTable(
		@RequestParam(name = "category", required = false) String category,
		@RequestParam(name = "infamous", required = false, defaultValue = "false") boolean infamous,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount,
		@RequestParam(name = "searchPhrase") String searchPhrase
	) {
		int pageSize = rowCount > 0 ? rowCount : MAX_RECORDS;
		RecordFilter filter = new RecordFilter(category, searchPhrase, infamous);
		return recordsService.getRecordsTable(filter, pageSize, current);
	}

	@RequestMapping("/recordTable")
	public BootgridTable<RecordDetailRow> recordTable(
		@RequestParam(name = "recordId") String recordId,
		@RequestParam(name = "active") boolean active,
		@RequestParam(name = "current") int current,
		@RequestParam(name = "rowCount") int rowCount
	) {
		int pageSize = rowCount > 0 ? rowCount : MAX_PLAYERS;
		return recordsService.getRecordTable(recordId, active, pageSize, current);
	}
}
