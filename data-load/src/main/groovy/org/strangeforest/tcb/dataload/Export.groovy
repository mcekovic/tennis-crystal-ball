package org.strangeforest.tcb.dataload

import java.text.*

import org.strangeforest.tcb.stats.model.core.*

import groovy.sql.*

def sqlPool = new SqlPool()

sqlPool.withSql { Sql sql ->
//	exportPlayers(sql)
//	exportRelevantPlayers(sql)
//	exportPlayerPerformance(sql)
//	exportPlayerStatistics(sql)
//	exportRankings(sql)
//	exportAllTournaments(sql)
//	new XMLTournamentExporter(sql).exportTournament('505', 2007)
}

def exportPlayers(Sql sql) {
	export(sql, 'players/players.json', /*language=SQL */ 'SELECT row_to_json(p) AS json FROM (SELECT * FROM player_v LEFT JOIN player_mapping USING (player_id) ORDER BY player_id) p')
}

def exportRelevantPlayers(Sql sql) {
	export(sql, 'players/players.json', /*language=SQL */ 'SELECT row_to_json(p) AS json FROM player_v p LEFT JOIN player_performance pp USING (player_id) WHERE goat_points > 0 OR best_rank > 0 OR matches_won + matches_lost > 0 ORDER BY player_id')
}

def exportPlayerPerformance(Sql sql) {
	export(sql, 'players/player-performance.json', /*language=SQL */ 'SELECT row_to_json(p) AS json FROM player_performance p ORDER BY player_id')
}

def exportPlayerStatistics(Sql sql) {
	export(sql, 'player-statistics.json', /*language=SQL */ 'SELECT row_to_json(p) AS json FROM player_stats p ORDER BY player_id')
	Surface.values().each { surface ->
		export(sql, "players/player-${surface.name().toLowerCase()}-statistics.json", /*language=SQL */ "SELECT row_to_json(p) AS json FROM player_surface_stats p WHERE surface = '${surface.code}' ORDER BY player_id")
	}
}

def exportRankings(Sql sql) {
	sql.rows('SELECT extract(YEAR FROM rank_date)::INTEGER AS season, rank_date FROM player_ranking GROUP BY rank_date ORDER BY rank_date').each {
		def season = it.season
		def date = it.rank_date
		export(sql, "rankings/$season/rankings-${formatDate date}.json", /*language=SQL */ 'SELECT row_to_json(r) AS json FROM (SELECT rank, player_id, name, country_id, rank_points FROM player_ranking INNER JOIN player_v USING (player_id) WHERE rank_date = :rankDate ORDER BY rank, player_id) r', [rankDate: date])
	}
}

def exportAllTournaments(Sql sql) {
	def exporter = new XMLTournamentExporter(sql)
	sql.rows('SELECT season, ext_tournament_id FROM tournament_event INNER JOIN tournament_mapping USING (tournament_id) ORDER BY date').each {
		exporter.exportTournament(it.ext_tournament_id, it.season, 'M:/TennisData/tournaments')
	}
}

def export(Sql sql, String fileName, String query, Map params = [:]) {
	print "Exporting to $fileName"
	def filePath = 'M:/TennisData/' + fileName
	new File(filePath).parentFile.mkdirs()
	def file = new File(filePath)
	file.delete()
	file << '[\n'
	int records = 0
	sql.eachRow(params, query, { p ->
		file << p.json.toString() + ',\n'
		if (++records % 1000 == 0)
			print '.'
	})
	file << ']'
	println()
}

private static String formatDate(Date date) {
	return new SimpleDateFormat('yyyy-MM-dd').format(date)
}





