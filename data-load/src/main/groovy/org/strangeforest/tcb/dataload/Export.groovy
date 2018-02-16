package org.strangeforest.tcb.dataload

import org.strangeforest.tcb.stats.model.core.Surface

import groovy.sql.Sql

def sqlPool = new SqlPool()

sqlPool.withSql { Sql sql ->
	export(sql, 'players.json', /*language=SQL */ 'SELECT row_to_json(p) AS json FROM player_v p LEFT JOIN player_performance pp USING (player_id) WHERE goat_points > 0 OR best_rank > 0 OR matches_won + matches_lost > 0 ORDER BY player_id' )
	export(sql, 'player-performance.json', /*language=SQL */ 'SELECT row_to_json(p) AS json FROM player_performance p ORDER BY player_id')
	export(sql, 'player-statistics.json', /*language=SQL */ 'SELECT row_to_json(p) AS json FROM player_stats p ORDER BY player_id')
	Surface.values().each { surface ->
		export(sql, "player-${surface.name().toLowerCase()}-statistics.json", /*language=SQL */ "SELECT row_to_json(p) AS json FROM player_surface_stats p WHERE surface = '${surface.code}' ORDER BY player_id")
	}
}

def export(Sql sql, String fileName, String query) {
	print "Exporting to $fileName"
	new File('export').mkdirs()
	def players = new File('export/' + fileName)
	players.delete()
	players << '[\n'
	int records = 0
	sql.eachRow(query, { p ->
		players << p.json.toString() + ',\n'
		if (++records % 1000 == 0)
			print '.'
	})
	players << ']'
	println()
}





