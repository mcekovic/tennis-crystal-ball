package org.strangeforest.tcb.stats.model.table;

import java.time.*;

import static java.lang.String.format;

public class DateRowCursor extends RowCursor<LocalDate> {

	public DateRowCursor(DataTable table, IndexedPlayers players) {
		super(table, players);
	}

	@Override protected String formatValue(LocalDate date) {
		return format("Date(%1$d, %2$d, %3$d)", date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
	}
}
