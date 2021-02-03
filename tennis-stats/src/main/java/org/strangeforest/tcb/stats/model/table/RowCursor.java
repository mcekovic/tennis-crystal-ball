package org.strangeforest.tcb.stats.model.table;

import java.util.*;

import static java.lang.String.*;

public abstract class RowCursor<T> {

	private final DataTable table;
	private final IndexedPlayers players;
	private T x;
	private String[] ys;

	RowCursor(DataTable table, IndexedPlayers players) {
		this.table = table;
		this.players = players;
	}

	public void next(T x, int playerId, Object y) {
		if (!Objects.equals(x, this.x)) {
			addRow();
			this.x = x;
			ys = new String[players.getCount()];
		}
		ys[players.getIndex(playerId)] = valueOf(y);
	}

	public void addRow() {
		if (x != null) {
			var row = table.addRow(formatValue(x));
			for (var y : ys)
				row.addCell(y);
			x = null;
		}
	}

	protected abstract String formatValue(T value);
}
