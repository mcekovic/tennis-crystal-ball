package org.strangeforest.tcb.stats.model.table;

import static java.lang.String.format;

public class DoubleRowCursor extends RowCursor<Double> {

	public DoubleRowCursor(DataTable table, IndexedPlayers players) {
		super(table, players);
	}

	@Override protected String formatValue(Double d) {
		return format("%1$.3f", d);
	}
}
