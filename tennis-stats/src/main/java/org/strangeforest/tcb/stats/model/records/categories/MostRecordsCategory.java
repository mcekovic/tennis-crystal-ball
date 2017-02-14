package org.strangeforest.tcb.stats.model.records.categories;

import org.strangeforest.tcb.stats.model.records.*;
import org.strangeforest.tcb.stats.model.records.details.*;

import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.records.categories.MostRecordsCategory.RecordType.*;

public class MostRecordsCategory extends RecordCategory {

	enum RecordType {
		FAMOUS(N_A, N_A, "NOT sr.infamous"),
		INFAMOUS("Infamous", "Infamous", "sr.infamous");

		private final String id;
		private final String name;
		private final String condition;

		RecordType(String id, String name, String condition) {
			this.id = id;
			this.name = name;
			this.condition = condition;
		}
	}

	private static final String RECORDS_WIDTH = "120";

	public MostRecordsCategory(boolean infamous) {
		super("Most " + (infamous ? "Infamous " : "") + "Records");
		register(mostRecords(infamous ? INFAMOUS : FAMOUS));
	}

	private static Record mostRecords(RecordType type) {
		return new Record(
			type.id + "Records", "Most " + suffix(type.name, " ") + "Records",
			/* language=SQL */
			"SELECT pr.player_id, count(DISTINCT record_id) AS value\n" +
			"FROM saved_record sr INNER JOIN player_record pr USING (record_id)\n" +
			"WHERE NOT sr.active_players AND pr.rank = 1 AND pr.record_id NOT IN ('Records', '" + INFAMOUS.id + "Records') AND " + type.condition + "\n" +
			"GROUP BY pr.player_id",
			"r.value", "r.value DESC", "r.value DESC", IntegerRecordDetail.class,
			asList(new RecordColumn("value", "numeric", null, RECORDS_WIDTH, "right", "Records"))
		);
	}
}
