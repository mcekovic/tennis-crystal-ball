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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RecordsITsConfig.class)
@Transactional
public class RecordsIT {

	@Autowired private RecordsService recordsService;
	@Autowired private PlayerService playerService;

	@Test
	public void testAllFamousRecords() {
		Map<Integer, Integer> records = new HashMap<>();
		for (RecordCategory recordCategory : Records.getRecordCategories()) {
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
		for (Map.Entry<Integer, Integer> entry : sortByValuesDesc(records).entrySet())
			System.out.printf("%30s %5s%n", playerService.getPlayerName(entry.getKey()), entry.getValue());
	}

	private static void incRecords(Map<Integer, Integer> records, int playerId) {
		Integer count = records.get(playerId);
		records.put(playerId, count != null ? count + 1 : 1);
	}

	public static <K, V extends Comparable<V>> Map<K, V> sortByValuesDesc(final Map<K, V> map) {
		Map<K, V> sortedByValues = new TreeMap<>((k1, k2) -> map.get(k2).compareTo(map.get(k1)));
	    sortedByValues.putAll(map);
	    return sortedByValues;
	}
}
