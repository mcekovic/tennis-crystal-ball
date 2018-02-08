package org.strangeforest.tcb.dataload

import org.springframework.jdbc.core.namedparam.*
import org.strangeforest.tcb.stats.service.*

import com.google.common.base.*
import groovy.sql.*

import static org.strangeforest.tcb.dataload.LoaderUtil.*

class WikipediaPlayerDataLoader {

	final Sql sql
	final PlayerService playerService

	static final int PROGRESS_LINE_WRAP = 100

	static final String FETCH_PLAYERS_FOR_DATA_UPDATE_SQL = //language=SQL
		'SELECT player_id FROM player_v\n' +
		'WHERE backhand IS NULL AND dob >= DATE \'1970-01-01\'\n' +
		'ORDER BY goat_points DESC NULLS LAST, dob DESC NULLS LAST'

	static final String UPDATE_PLAYER_DATA_SQL = //language=SQL
		'UPDATE player\n' +
		'SET backhand = :backhand::player_backhand\n' +
		'WHERE player_id = :playerId'


	WikipediaPlayerDataLoader(Sql sql) {
		this.sql = sql
		playerService = new PlayerService(new NamedParameterJdbcTemplate(SqlPool.dataSource()))
	}

	def updatePlayerData() {
		def stopwatch = Stopwatch.createStarted()
		println 'Updating player data'
		def playerIds = sql.rows(FETCH_PLAYERS_FOR_DATA_UPDATE_SQL).collect { row -> row.player_id }
		def updates = 0, errors = 0
		playerIds.each { playerId ->
			try {
				def url = playerService.getPlayerWikipediaUrl(playerId)
				def backhand = findBackhand(url)
				if (backhand) {
					updatePlayer(playerId, backhand)
					++updates
					print '.'
				}
				else {
					++errors
					print '!'
				}
				if ((updates + errors) % PROGRESS_LINE_WRAP == 0)
					println()
			}
			catch (Exception ex) {
				ex.printStackTrace()
			}
		}
		println "\n$updates players updated in $stopwatch, cannot update $errors players"
	}

	static def findBackhand(String url) {
		def doc = retriedGetDoc(url)
		def playsTr = doc.select('table.infobox.vcard > tbody > tr').findResult { tr -> tr.select('th').text().toLowerCase() == 'plays' ? tr : null }
		if (playsTr) {
			def plays = playsTr.select('td:first-of-type').text().toLowerCase()
			if (plays.contains('two') || plays.contains('double') || plays.contains('2'))
				return '2'
			else if (plays.contains('one') || plays.contains('single') || plays.contains('1'))
				return '1'
		}
		null
	}

	def updatePlayer(playerId, backhand) {
		sql.execute([playerId: playerId, backhand: backhand], UPDATE_PLAYER_DATA_SQL)
		sql.commit()
	}
}