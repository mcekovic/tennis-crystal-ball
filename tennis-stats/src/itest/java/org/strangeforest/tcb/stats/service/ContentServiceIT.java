package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.strangeforest.tcb.stats.boot.*;
import org.strangeforest.tcb.stats.model.records.*;

import static org.assertj.core.api.Assertions.*;

@ServiceTest
class ContentServiceIT {

	@Autowired private ContentService contentService;

	@Test
	void testAllRecordsOfTheDay() {
		Map<Record, Integer> records = new HashMap<>();
		for (int day = 1; day <= 365; day++) {
			RecordOfTheDay recordOfTheDay = contentService.getRecordOfTheDay(day);
			records.compute(recordOfTheDay.getRecord(), (r, c) -> c != null ? c + 1 : 1);
		}
		assertThat(records.values().stream().mapToInt(c -> c).max().orElseThrow()).isLessThanOrEqualTo(13);
	}
}
