package org.strangeforest.tcb.stats.records;

import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;

import static java.util.Comparator.*;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = RecordsITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
class RecordsIT {

	@Autowired private RecordsService recordsService;
	@Autowired private PlayerService playerService;

	@BeforeAll
	void setUp() {
		recordsService.clearRecords();
	}

	@Test
	void testAllFamousRecords() {
		testRecords(Records.getRecordCategories(), false);
	}

	@Test
	void testAllInfamousRecords() {
		testRecords(Records.getInfamousRecordCategories(), false);
	}

	@Test
	void testAllFamousRecordsForActivePlayers() {
		testRecords(Records.getRecordCategories(), true);
	}

	@Test
	void testAllInfamousRecordsForActivePlayers() {
		testRecords(Records.getInfamousRecordCategories(), true);
	}

	private void testRecords(List<RecordCategory> categories, boolean active) {
		Map<Integer, PlayerRecords> records = new HashMap<>();
		for (RecordCategory recordCategory : categories) {
			for (Record record : recordCategory.getRecords()) {
				BootgridTable<RecordDetailRow> table = recordsService.getRecordTable(record.getId(), active, 100, 1);
				if (table.getRowCount() > 0) {
					for (RecordDetailRow row : table.getRows()) {
						if (row.getRank() == 1)
							incRecords(records, row.getPlayerId());
						else
							break;
					}
				}
			}
		}
		records.values().stream().sorted(reverseOrder()).forEach(record ->
			System.out.printf("%30s %5s%n", playerService.getPlayerName(record.playerId), record.records)
		);
	}

	private static void incRecords(Map<Integer, PlayerRecords> records, int playerId) {
		PlayerRecords playerRecords = records.get(playerId);
		if (playerRecords != null)
			playerRecords.records++;
		else
			records.put(playerId, new PlayerRecords(playerId));
	}

	private static class PlayerRecords implements Comparable<PlayerRecords> {

		private int playerId;
		private int records;

		PlayerRecords(int playerId) {
			this.playerId = playerId;
			records = 1;
		}

		@Override public int compareTo(PlayerRecords pr) {
			return Integer.compare(records, pr.records);
		}
	}
}
