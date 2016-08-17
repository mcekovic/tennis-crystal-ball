package org.strangeforest.tcb.stats.records;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.testng.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.service.*;
import org.testng.annotations.*;

@ContextConfiguration(classes = RecordsITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
public class RecordsLoaderIT extends AbstractTestNGSpringContextTests {

	@Autowired private RecordsService recordsService;

	@Test
	public void loadAllFamousRecords() {
		loadRecords(Records.getRecordCategories());
	}

	@Test
	public void loadAllInfamousRecords() {
		loadRecords(Records.getInfamousRecordCategories());
	}

	private void loadRecords(List<RecordCategory> categories) {
		for (RecordCategory recordCategory : categories) {
			for (Record record : recordCategory.getRecords())
				recordsService.refreshRecord(record.getId(), false);
		}
	}
}
