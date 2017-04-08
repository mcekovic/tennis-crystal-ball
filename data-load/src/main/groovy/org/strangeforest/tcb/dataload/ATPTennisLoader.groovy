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
				for (decade in ['70s', '80s', '90s', '00s-mc', '10s-mc'])
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
				for (year in 1968..2016)
					rows += loader.loadFile(baseDir() + "atp_matches_${year}.csv")
			}
			def year = 2017
			rows += loader.loadFile(baseDir() + "atp_matches_${year}.csv")
		}
		println()
	}

	def loadMatchPrices(loader) {
		println 'Loading match prices'
		load {
			def rows = 0
			if (full) {
				for (year in 2001..2016)
					rows += loader.loadFile(baseDir() + "match_prices_${year}.csv")
			}
			def year = 2017
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
		if (full) {
			loadAdditionalData(new AdditionalPlayerDataLoader(sql), 'player', 'classpath:/player-data.xml')

			println 'Adding player aliases and missing players...'
			executeSQLFile(sql, 'src/main/db/player-aliases-missing-players.sql')
		}
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
			def atpWorldTourMatchLoader = new ATPWorldTourTournamentLoader(sql)
			atpWorldTourMatchLoader.loadTournament(1968, 'manchester', 7310, false, null, null, ['R32', 'R64'])
			atpWorldTourMatchLoader.loadTournament(1968, 'london', 311)
			atpWorldTourMatchLoader.loadTournament(1968, 'dublin', 2029, false, null, 'G', ['R32'])
			atpWorldTourMatchLoader.loadTournament(1968, 'buenos-aires', 303, false, null, null, ['R32', 'R64'])
			atpWorldTourMatchLoader.loadTournament(1969, 'perth', 243, false, null, 'G', ['R16', 'R32'])
			atpWorldTourMatchLoader.loadTournament(1969, 'hobart', 713, false, null, 'G')
			atpWorldTourMatchLoader.loadTournament(1969, 'new-york', 2058, false, null, 'P')
			atpWorldTourMatchLoader.loadTournament(1969, 'durban', 260, false, null, null, ['R32', 'R64'])
			atpWorldTourMatchLoader.loadTournament(1969, 'brussels', 406, false, null, 'C')
			atpWorldTourMatchLoader.loadTournament(1969, 'dublin', 2029, false, null, 'G', ['R32'])
			atpWorldTourMatchLoader.loadTournament(1969, 'las-vegas', 413, false, null, 'H')
			atpWorldTourMatchLoader.loadTournament(1970, 'north-miami-beach', 681)
			atpWorldTourMatchLoader.loadTournament(1970, 'richmond', 802, false, null, 'H')
			atpWorldTourMatchLoader.loadTournament(1970, 'salisbury', 355)
			atpWorldTourMatchLoader.loadTournament(1970, 'chorpus-christi', 6816)
			atpWorldTourMatchLoader.loadTournament(1970, 'los-angeles', 6811)
			atpWorldTourMatchLoader.loadTournament(1970, 'macon', 2066)
			atpWorldTourMatchLoader.loadTournament(1970, 'szczecin', 657, false, null, 'P')
			atpWorldTourMatchLoader.loadTournament(1970, 'wembley', 6812)
			atpWorldTourMatchLoader.loadTournament(1970, 'san-juan', 6817, false, null, null, ['R64'])
			atpWorldTourMatchLoader.loadTournament(1970, 'durban', 260, false, null, null, ['R64'])
			atpWorldTourMatchLoader.loadTournament(1970, 'st-petersburg', 6818, false, null, null, ['R64'])
			atpWorldTourMatchLoader.loadTournament(1970, 'bermuda', 436)
			atpWorldTourMatchLoader.loadTournament(1970, 'dallas', 388, false, null, 'P')
			atpWorldTourMatchLoader.loadTournament(1970, 'houston', 405, false, null, 'H')
			atpWorldTourMatchLoader.loadTournament(1970, 'bournemouth', 347)
			atpWorldTourMatchLoader.loadTournament(1970, 'atlanta', 412, false, null, 'H')
			atpWorldTourMatchLoader.loadTournament(1970, 'las-vegas', 413, false, null, 'H')
			atpWorldTourMatchLoader.loadTournament(1970, 'brussels', 406, false, null, 'C')
			atpWorldTourMatchLoader.loadTournament(1970, 'manchester', 7310)
			atpWorldTourMatchLoader.loadTournament(1970, 'bristol', 313)
			atpWorldTourMatchLoader.loadTournament(1970, 'nottingham', 6829, false, null, null, ['R32', 'R64'])
			atpWorldTourMatchLoader.loadTournament(1970, 'eastbourne', 2049)
			atpWorldTourMatchLoader.loadTournament(1970, 'newport', 2050, false, null, null, ['R32', 'R64'])
			atpWorldTourMatchLoader.loadTournament(1970, 'dublin', 2029, false, null, 'G', ['R32'])
			atpWorldTourMatchLoader.loadTournament(1970, 'hoylake', 6830, false, null, null, ['R32', 'R64'])
			atpWorldTourMatchLoader.loadTournament(1970, 'leicester', 6831)
			atpWorldTourMatchLoader.loadTournament(1970, 'munich', 804, false, null, 'C')
			atpWorldTourMatchLoader.loadTournament(1970, 'fort-worth', 653)
			atpWorldTourMatchLoader.loadTournament(1970, 'merion', 2030, false, null, 'H')
			atpWorldTourMatchLoader.loadTournament(1970, 'vancouver', 2048)
			atpWorldTourMatchLoader.loadTournament(1970, 'midland', 6832)
			atpWorldTourMatchLoader.loadTournament(1970, 'tucson', 6833)
			atpWorldTourMatchLoader.loadTournament(1970, 'antwerp', 430, false, 'M', 'P')
			atpWorldTourMatchLoader.loadTournament(1971, 'bristol', 313)
			atpWorldTourMatchLoader.loadTournament(1971, 'eastbourne', 2049)
			atpWorldTourMatchLoader.loadTournament(1971, 'senigallia', 6834, false, null, null, ['R64'])
			atpWorldTourMatchLoader.loadTournament(1972, 'macon', 2066)
			atpWorldTourMatchLoader.loadTournament(1972, 'tokyo', 329, false, null, null, ['R32'])
			atpWorldTourMatchLoader.loadTournament(1973, 'charleston', 6945)
			atpWorldTourMatchLoader.loadTournament(1974, 'lakeway', 6836)
			atpWorldTourMatchLoader.loadTournament(1974, 'hong-kong', 336)
			atpWorldTourMatchLoader.loadTournament(2007, 'vina-del-mar', 505)
			atpWorldTourMatchLoader.loadTournament(2008, 'moscow', 438)

			loadAdditionalMatchData(sql, 'classpath:/tournaments/1969-fort-worth.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1969-johannesburg.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1969-los-angeles.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1969-wembley.xml')
//			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-fort-worth.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-johannesburg.xml')
//			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-salisbury.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-sydney.xml')
			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-tennis-champions-classic.xml')
//			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-vancouver.xml')
//			loadAdditionalMatchData(sql, 'classpath:/tournaments/1970-wembley.xml')
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

			println 'Updating tournament event surfaces...'
			executeSQLFile(sql, 'src/main/db/tournament-event-surfaces.sql')

			println 'Loading team tournament winners...'
			executeSQLFile(sql, 'src/main/db/team-tournament-winners.sql')
		}
		def stopwatch = Stopwatch.createStarted()
		println 'Correcting data (delta)...'
		executeSQLFile(sql, 'src/main/db/correct-data-delta.sql')
		println "Correcting data (delta) finished in $stopwatch\n"

		println 'Updating tournament event map properties...'
		executeSQLFile(sql, 'src/main/db/tournament-map-properties.sql')
	}

	def refreshMaterializedViews(Sql sql) {
		def stopwatch = Stopwatch.createStarted()
		refreshMaterializedView(sql, 'player_current_rank')
		refreshMaterializedView(sql, 'player_best_rank')
		refreshMaterializedView(sql, 'player_best_rank_points')
		refreshMaterializedView(sql, 'player_year_end_rank')
		refreshMaterializedView(sql, 'player_current_elo_rank')
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
