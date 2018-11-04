package org.strangeforest.tcb.stats.records;

import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.service.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RecordsITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
class RecordsLoaderIT {

	@Autowired private RecordsService recordsService;

	@Test
	void loadAllFamousRecords() {
		loadRecords(Records.getRecordCategories());
	}

	@Test
	void loadAllInfamousRecords() {
		loadRecords(Records.getInfamousRecordCategories());
	}

	private void loadRecords(List<RecordCategory> categories) {
		for (RecordCategory recordCategory : categories) {
			for (Record record : recordCategory.getRecords())
				recordsService.refreshRecord(record.getId(), false);
		}
	}
}
