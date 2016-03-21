package org.strangeforest.tcb.stats.model.table;

import static java.lang.String.valueOf;

public class IntegerRowCursor extends RowCursor<Integer> {

	public IntegerRowCursor(DataTable table, IndexedPlayers players) {
		super(table, players);
	}

	@Override protected String formatValue(Integer i) {
		return valueOf(i);
	}
}
