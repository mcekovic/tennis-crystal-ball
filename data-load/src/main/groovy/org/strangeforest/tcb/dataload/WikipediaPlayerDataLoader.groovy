package org.strangeforest.tcb.dataload

import java.util.concurrent.*
import java.util.concurrent.atomic.*

import org.springframework.jdbc.core.namedparam.*
import org.strangeforest.tcb.stats.service.*

import com.google.common.base.*

import static org.strangeforest.tcb.dataload.LoaderUtil.*

class WikipediaPlayerDataLoader {

	final SqlPool sqlPool
	final PlayerService playerService

	static final int PROGRESS_LINE_WRAP = 100
	static final int FETCH_THREAD_COUNT = 5

	static final String FETCH_PLAYERS_FOR_DATA_UPDATE_SQL = //language=SQL
		'SELECT player_id FROM player_v\n' +
		'WHERE (wikipedia IS NULL OR backhand IS NULL OR turned_pro IS NULL OR prize_money IS NULL OR web_site IS NULL)\n' +
		'AND (goat_points > 0 OR best_rank <= 500)\n' +
		'ORDER BY goat_points DESC, best_rank'

	WikipediaPlayerDataLoader(SqlPool sqlPool) {
		this.sqlPool = sqlPool
		playerService = new PlayerService(new NamedParameterJdbcTemplate(SqlPool.dataSource()))
	}

	def updatePlayerData() {
		def stopwatch = Stopwatch.createStarted()
		println 'Updating player data'
		def playerIds = sqlPool.withSql {
			sql -> sql.rows(FETCH_PLAYERS_FOR_DATA_UPDATE_SQL).collect { row -> row.player_id }
		}
		def updates = new AtomicInteger(), errors = new AtomicInteger(), total = new AtomicInteger()
		ForkJoinPool pool = new ForkJoinPool(FETCH_THREAD_COUNT)
		try {
			pool.submit{
				playerIds.parallelStream().forEach { playerId ->
					try {
						def url = playerService.getPlayerWikipediaUrl(playerId)
						def playerData = findPlayerData(url)
						if (playerData) {
							updatePlayer(playerId, playerData)
							updates.incrementAndGet()
							print '.'
						}
						else {
							errors.incrementAndGet()
							print '!'
						}
						if (total.incrementAndGet() % PROGRESS_LINE_WRAP == 0)
							printf(' %1$.2f%%\n', 100.0 * total.get() / playerIds.size())
					}
					catch (Exception ex) {
						System.err.println 'Error finding player data for player: ' + playerId
						ex.printStackTrace()
					}
				}
			}.get()
		}
		finally {
			pool.shutdown()
		}

		println "\n$updates players updated in $stopwatch, cannot update $errors players"
	}

	static def findPlayerData(String url) {
		def playerData = [:]
		playerData['wikipedia'] = url

		def doc = retriedGetDoc(url)
		def vcard = doc.select('table.infobox.vcard > tbody')

//		// Birthplace
//		def born = findVCardField(vcard, 'born')
//		if (born) {
//			def pos = born.lastIndexOf(')')
//			if (pos > 0)
//				playerData['birthplace'] = born.substring(pos + 1).trim()
//		}
//
//		// Residence
//		def residence = findVCardField(vcard, 'residence')
//		if (residence)
//			playerData['residence'] = residence

		// Plays
		def plays = findVCardField(vcard, 'plays')
		if (plays) {
			plays = plays.toLowerCase()
			if (plays.contains('two') || plays.contains('double') || plays.contains('2'))
				playerData['backhand'] = '2'
			else if (plays.contains('one') || plays.contains('single') || plays.contains('1'))
				playerData['backhand'] = '1'
		}

		// Turned Pro
		def turnedPro = findVCardField(vcard, 'turned')
		if (turnedPro)
			playerData['turned_pro'] = safeInteger turnedPro.replaceAll('\\(.*\\)', '').replaceAll('\\[.*]', '').trim()

		// Prize Money
		def prizeMoney = findVCardField(vcard, 'prize')
		if (prizeMoney) {
			def pos = prizeMoney.indexOf(' ' + (char)160)
			if (pos > 0)
				prizeMoney = prizeMoney.substring(0, pos)
			if (prizeMoney.matches('.*\\d+.*'))
				playerData['prize_money'] = prizeMoney.replaceAll('\\(.*\\)', '').replaceAll('\\[.*]', '').trim()
		}

		// Website
		def website = findVCardField(vcard, 'website')
		if (website)
			playerData['web_site'] = website

		playerData
	}

	static String findVCardField(vcard, String name) {
		def valueTr = vcard.select('tr').findResult { tr -> tr.select('th').text().toLowerCase().contains(name) ? tr : null }
		def text = valueTr?.select('td:first-of-type')?.text()
		text?.trim()?.replaceAll('\\s', ' ')
	}

	def updatePlayer(int playerId, Map params) {
		def paramsSql = params.keySet().collect{ param -> "$param = :$param" + (param == 'backhand' ? '::player_backhand' : '') }.join(', ')
		def paramMap = [playerId: playerId] << params
		sqlPool.withSql { sql ->
			sql.execute(paramMap, "UPDATE player SET $paramsSql WHERE player_id = :playerId")
		}
	}

	static Integer safeInteger(i) {
		try {
			i ? i.toInteger() : null
		}
		catch (Exception ex) {
			null
		}
	}
}