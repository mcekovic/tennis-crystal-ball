package org.strangeforest.tcb.stats.records;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.table.*;
import org.strangeforest.tcb.stats.service.*;

import static java.util.Comparator.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RecordsITsConfig.class)
@Transactional
public class RecordsIT {

	@Autowired private RecordsService recordsService;
	@Autowired private PlayerService playerService;

	@Test
	public void testAllFamousRecords() {
		testRecords(Records.getRecordCategories());
	}

	@Test
	public void testAllInfamousRecords() {
		testRecords(Records.getInfamousRecordCategories());
	}

	private void testRecords(List<RecordCategory> categories) {
		Map<Integer, PlayerRecords> records = new HashMap<>();
		for (RecordCategory recordCategory : categories) {
			for (Record record : recordCategory.getRecords()) {
				BootgridTable<RecordRow> table = recordsService.getRecordTable(record.getId(), false, 100, 1);
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
