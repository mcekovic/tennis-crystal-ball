package org.strangeforest.tcb.dataload

import java.util.concurrent.*

import com.google.common.base.*
import groovy.sql.*

class ATPTennisLoader {

	private final boolean full
	private final boolean useMaterializedViews
	private String baseDir

	ATPTennisLoader() {
		full = System.getProperty('tcb.data.full-load', 'true').toBoolean()
		useMaterializedViews = System.getProperty('tcb.data.use-materialized-views', 'true').toBoolean()
	}

	def loadPlayers(loader) {
		println 'Loading players'
		loader.loadFile(baseDir() + 'atp_players.csv')
		println()
	}

	def loadRankings(loader) {
		println 'Loading rankings'
		load {
			def rows = 0
			if (full) {
				for (decade in ['70s', '80s', '90s', '00s-mc', '10s'])
					rows += loader.loadFile(baseDir() + "atp_rankings_${decade}.csv")
			}
			rows += loader.loadFile(baseDir() + "atp_rankings_current.csv")
		}
		println()
	}

	def loadMatches(loader) {
		println 'Loading matches'
		load {
			def rows = 0
			if (full) {
				for (year in 1968..2015)
					rows += loader.loadFile(baseDir() + "atp_matches_${year}.csv")
			}
			def year = 2016
			rows += loader.loadFile(baseDir() + "atp_matches_${year}.csv")
		}
		println()
	}

	def loadMatchPrices(loader) {
		println 'Loading match prices'
		load {
			def rows = 0
			if (full) {
				for (year in 2001..2015)
					rows += loader.loadFile(baseDir() + "match_prices_${year}.csv")
			}
			def year = 2016
			rows += loader.loadFile(baseDir() + "match_prices_${year}.csv")
		}
		println()
	}

	private static load(loader) {
		def stopwatch = Stopwatch.createStarted()

		def rows = loader()

		stopwatch.stop()
		def seconds = stopwatch.elapsed(TimeUnit.SECONDS)
		int rowsPerSecond = seconds ? rows / seconds : 0
		println "Total rows: $rows in $stopwatch ($rowsPerSecond row/s)"
	}

	private baseDir() {
		if (!baseDir) {
			baseDir = System.properties['tcb.data.base-dir']
			if (!baseDir)
				throw new IllegalArgumentException('No Tennis data base directory is set, please specify it in tcb.data.base-dir system property.')
			if (!baseDir.endsWith(File.separator))
				baseDir += File.separator
		}
		return baseDir
	}

	def loadAdditionalPlayerData(Sql sql) {
		if (full)
			loadAdditionalData(new AdditionalPlayerDataLoader(sql), 'player', 'classpath:/player-data.xml')
	}

	def loadAdditionalRankingData(Sql sql) {
		if (full) {
			println 'Loading missing rankings...'
			executeSQLFile(sql, 'src/main/db/missing-atp-rankings.sql')
			println 'Loading pre-ATP rankings...'
			executeSQLFile(sql, 'src/main/db/rankings-pre-atp.sql')
		}
	}

	def loadAdditionalTournamentData(Sql sql) {
		if (full) {
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1969-fort-worth.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1969-johannesburg.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1969-los-angeles.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1969-wembley.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-fort-worth.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-johannesburg.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-salisbury.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-sydney.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-tennis-champions-classic.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-vancouver.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-wembley.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1971-johannesburg.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1971-tennis-champions-classic.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1972-roanoke.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1973-washington-indoor-2.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1974-auckland.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1977-johannesburg.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1977-johannesburg-2.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1979-dorado-beach.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1981-monte-carlo.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1984-memphis+.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1984-rotterdam.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1987-stratton-mountain.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1990-dusseldorf.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1990-tour-finals.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1999-tour-finals+.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/2000-dusseldorf+.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/2000-tour-finals+.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/2002-tour-finals+.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/2003-tour-finals+.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/2004-tour-finals+.xml')
		}
	}

	static loadAdditionalTournament(SqlPool sqlPool, String file) {
		sqlPool.withSql { sql ->
			loadAdditionalMatchData(sql, file)
		}
	}

	private static loadAdditionalMatchData(Sql sql, String file) {
		loadAdditionalData(new XMLMatchLoader(sql), 'match', file)
	}

	private static loadAdditionalData(loader, String name, String file) {
		println "Loading additional $name data"
		loader.loadFile(file)
		println()
	}

	def correctData(sql) {
		if (full) {
			def stopwatch = Stopwatch.createStarted()
			println 'Correcting data (full)...'
			executeSQLFile(sql, 'src/main/db/correct-data-full.sql')
			println "Correcting data (full) finished in $stopwatch"
		}
		def stopwatch = Stopwatch.createStarted()
		println 'Correcting data (delta)...'
		executeSQLFile(sql, 'src/main/db/correct-data-delta.sql')
		println "Correcting data (delta) finished in $stopwatch\n"
		if (full) {
			stopwatch = Stopwatch.createStarted()
			println 'Updating tournament event map properties...'
			executeSQLFile(sql, 'src/main/db/tournament-map-properties.sql')
			println "Updating tournament event map properties finished in $stopwatch"
		}
	}

	def refreshMaterializedViews(Sql sql) {
		def stopwatch = Stopwatch.createStarted()
		refreshMaterializedView(sql, 'player_current_rank')
		refreshMaterializedView(sql, 'player_best_rank')
		refreshMaterializedView(sql, 'player_best_rank_points')
		refreshMaterializedView(sql, 'player_year_end_rank')
		refreshMaterializedView(sql, 'player_best_elo_rank')
		refreshMaterializedView(sql, 'player_best_elo_rating')
		refreshMaterializedView(sql, 'player_year_end_elo_rank')
		refreshMaterializedView(sql, 'player_season_weeks_at_no1')
		refreshMaterializedView(sql, 'player_weeks_at_no1')
		refreshMaterializedView(sql, 'event_participation')
		refreshMaterializedView(sql, 'player_tournament_event_result')
		refreshMaterializedView(sql, 'player_titles')
		refreshMaterializedView(sql, 'player_season_performance')
		refreshMaterializedView(sql, 'player_tournament_performance')
		refreshMaterializedView(sql, 'player_performance')
		refreshMaterializedView(sql, 'player_season_surface_stats')
		refreshMaterializedView(sql, 'player_season_stats')
		refreshMaterializedView(sql, 'player_surface_stats')
		refreshMaterializedView(sql, 'player_stats')
		refreshMaterializedView(sql, 'player_h2h')
		refreshMaterializedView(sql, 'player_win_streak')
		refreshMaterializedView(sql, 'player_surface_win_streak')
		refreshMaterializedView(sql, 'player_level_win_streak')
		refreshMaterializedView(sql, 'player_vs_no1_win_streak')
		refreshMaterializedView(sql, 'player_vs_top5_win_streak')
		refreshMaterializedView(sql, 'player_vs_top10_win_streak')
		refreshMaterializedView(sql, 'player_tournament_win_streak')
		refreshMaterializedView(sql, 'player_tournament_level_win_streak')
		refreshMaterializedView(sql, 'player_season_goat_points')
		refreshMaterializedView(sql, 'player_goat_points')
		println "\nMaterialized views refreshed in $stopwatch"
	}

	def refreshMaterializedView(Sql sql, String viewName) {
		def stopwatch = Stopwatch.createStarted()
		println "Refreshing materialized view '$viewName'"
		if (useMaterializedViews)
			sql.execute("REFRESH MATERIALIZED VIEW $viewName".toString())
		else {
			sql.execute("DELETE FROM $viewName".toString())
			sql.execute("INSERT INTO $viewName SELECT * FROM ${viewName}_v".toString())
		}
		sql.commit()
		println "Materialized view '$viewName' refreshed in $stopwatch"
	}

	def createDatabase(Sql sql) {
		def stopwatch = Stopwatch.createStarted()

		println 'Creating types...'
		executeSQLFile(sql, '../crystal-ball/src/main/db/create-types.sql')

		println 'Creating tables...'
		executeSQLFile(sql, '../crystal-ball/src/main/db/create-tables.sql')

		println 'Creating functions...'
		executeSQLFile(sql, '../crystal-ball/src/main/db/create-functions.sql')

		println 'Creating views...'
		if (useMaterializedViews)
			executeSQLFile(sql, '../crystal-ball/src/main/db/create-views.sql')
		else
			executeSQLFile(sql, '../crystal-ball/src/main/db/create-views.sql', 'MATERIALIZED VIEW', 'TABLE')

		println 'Loading initial data...'
		executeSQLFile(sql, '../crystal-ball/src/main/db/initial-load.sql')

		println 'Creating load functions...'
		executeSQLFile(sql, 'src/main/db/load-functions.sql')

		println "Database created in $stopwatch"
	}

	def dropDatabase(Sql sql) {
		def stopwatch = Stopwatch.createStarted()

		println 'Dropping load functions...'
		executeSQLFile(sql, 'src/main/db/drop-load-functions.sql')

		println 'Dropping views...'
		if (useMaterializedViews)
			executeSQLFile(sql, '../crystal-ball/src/main/db/drop-views.sql')
		else
			executeSQLFile(sql, '../crystal-ball/src/main/db/drop-views.sql', 'MATERIALIZED VIEW', 'TABLE')

		println 'Dropping functions...'
		executeSQLFile(sql, '../crystal-ball/src/main/db/drop-functions.sql')

		println 'Dropping tables...'
		executeSQLFile(sql, '../crystal-ball/src/main/db/drop-tables.sql')

		println 'Dropping types...'
		executeSQLFile(sql, '../crystal-ball/src/main/db/drop-types.sql')

		println "Database dropped in $stopwatch"
	}

	def vacuum(Sql sql) {
		def stopwatch = Stopwatch.createStarted()

		println 'Vacuuming tables and materialized views...'
		executeSQLFileWithAutoCommit(sql, '../crystal-ball/src/main/db/vacuum.sql')

		println "Vacuuming finished in $stopwatch"
	}

	private static executeSQLFile(Sql sql, String file, String replaceTarget = null, String replacement = null) {
		def sqlText = new File(file).text
		if (replaceTarget && replacement)
			sqlText = sqlText.replace(replaceTarget, replacement)
		sql.execute(sqlText)
		sql.commit()
	}

	private static executeSQLFileWithAutoCommit(Sql sql, String file) {
		def sqlText = new File(file).text
		sql.connection.autoCommit = true
		try {
			sql.execute(sqlText)
		}
		finally {
			sql.connection.autoCommit = false
		}
	}
}
