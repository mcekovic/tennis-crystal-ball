package org.strangeforest.tcb.dataload

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
		println 'Loading ATP players'
		loader.loadFile(baseDir() + 'atp_players.csv')
		println()
	}

	def loadRankings(loader) {
		println 'Loading ATP rankings'
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
		println 'Loading ATP matches'
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

	private static load(loader) {
		def t0 = System.currentTimeMillis()

		def rows = loader()

		def seconds = (System.currentTimeMillis() - t0) / 1000.0
		int rowsPerSecond = rows / seconds
		println "Total rows: $rows in $seconds s ($rowsPerSecond row/s)"
	}

	private baseDir() {
		if (!baseDir) {
			baseDir = System.properties['tcb.data.base-dir']
			if (!baseDir)
				throw new IllegalArgumentException('No ATP Tennis data base directory is set, please specify it in tcb.data.base-dir system property.')
			if (!baseDir.endsWith(File.separator))
				baseDir += File.separator
		}
		return baseDir
	}

	def loadAdditionalPlayerData(sql) {
		if (full)
			loadAdditionalData(new AdditionalPlayerDataLoader(sql), 'player', 'classpath:/player-data.xml')
	}

	def loadAdditionalTournamentData(sql) {
		if (full) {
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1969-johannesburg.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1969-wembley.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1970-johannesburg.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1970-salisbury.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1970-sydney.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1970-wembley.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1971-johannesburg.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1972-roanoke.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1973-washington-indoor-2.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1974-auckland.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1977-johannesburg.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1977-johannesburg-2.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1979-dorado-beach.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1981-monte-carlo.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1984-memphis+.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1984-rotterdam.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1987-stratton-mountain.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1990-dusseldorf.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1990-tour-finals.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1999-tour-finals+.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/2000-dusseldorf+.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/2000-tour-finals+.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/2002-tour-finals+.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/2003-tour-finals+.xml')
			loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/2004-tour-finals+.xml')
		}
	}

	public static loadAdditionalTournament(sqlPool, file) {
		sqlPool.withSql { sql ->
			loadAdditionalData(new XMLMatchLoader(sql), 'match', file)
		}
	}

	private static loadAdditionalData(loader, name, file) {
		println "Loading additional $name data"
		loader.loadFile(file)
		println()
	}

	def refreshComputedData(Sql sql) {
		if (useMaterializedViews)
			refreshMaterializedViews(sql)
		else
			refreshMaterializedViewTables(sql)
	}

	static refreshMaterializedViews(Sql sql) {
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
		refreshMaterializedView(sql, 'player_win_streak')
		refreshMaterializedView(sql, 'player_surface_win_streak')
		refreshMaterializedView(sql, 'player_level_win_streak')
		refreshMaterializedView(sql, 'player_vs_no1_win_streak')
		refreshMaterializedView(sql, 'player_vs_top5_win_streak')
		refreshMaterializedView(sql, 'player_vs_top10_win_streak')
		refreshMaterializedView(sql, 'player_season_goat_points')
		refreshMaterializedView(sql, 'player_goat_points')
	}

	private static refreshMaterializedView(Sql sql, String viewName) {
		def t0 = System.currentTimeMillis()
		println "Refreshing materialized view '$viewName'"
		sql.execute('REFRESH MATERIALIZED VIEW ' + viewName)
		sql.commit()
		def seconds = (System.currentTimeMillis() - t0) / 1000.0
		println "Materialized view '$viewName' refreshed in $seconds s"
	}

	private static refreshMaterializedViewTables(Sql sql) {
		def t0 = System.currentTimeMillis()
		println 'Dropping views...'
		executeSQLFile(sql, '../crystal-ball/src/main/db/drop-views.sql', 'MATERIALIZED VIEW', 'TABLE')
		println 'Creating views...'
		executeSQLFile(sql, '../crystal-ball/src/main/db/create-views.sql', 'MATERIALIZED VIEW', 'TABLE')
		def seconds = (System.currentTimeMillis() - t0) / 1000.0
		println "Materialized view tables refreshed in $seconds s"
	}

	private static executeSQLFile(Sql sql, String file, String replaceTarget, String replacement) {
		sql.execute(new File(file).text.replace(replaceTarget, replacement))
		sql.commit()
	}
}
