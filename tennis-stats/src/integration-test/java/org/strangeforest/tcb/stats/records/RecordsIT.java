package org.strangeforest.tcb.stats.records;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.test.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.testng.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;
import org.testng.annotations.*;

import static java.util.Comparator.*;

@ContextConfiguration(classes = RecordsITsConfig.class)
@Transactional @Commit
public class RecordsIT extends AbstractTransactionalTestNGSpringContextTests {

	@Autowired private RecordsService recordsService;
	@Autowired private PlayerService playerService;

	@BeforeClass
	public void setUp() {
		recordsService.clearRecords();
	}

	@Test
	public void testAllFamousRecords() {
		testRecords(Records.getRecordCategories(), false);
	}

	@Test
	public void testAllInfamousRecords() {
		testRecords(Records.getInfamousRecordCategories(), false);
	}

	@Test
	public void testAllFamousRecordsForActivePlayers() {
		testRecords(Records.getRecordCategories(), true);
	}

	@Test
	public void testAllInfamousRecordsForActivePlayers() {
		testRecords(Records.getInfamousRecordCategories(), true);
	}

	private void testRecords(List<RecordCategory> categories, boolean active) {
		Map<Integer, PlayerRecords> records = new HashMap<>();
		for (RecordCategory recordCategory : categories) {
			for (Record record : recordCategory.getRecords()) {
				BootgridTable<RecordRow> table = recordsService.getRecordTable(record.getId(), active, 100, 1);
				if (table.getRowCount() > 0) {
					for (RecordRow row : table.getRows()) {
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
