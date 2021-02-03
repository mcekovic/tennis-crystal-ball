package org.strangeforest.tcb.dataload

import java.time.*
import java.util.concurrent.*

import org.springframework.jdbc.core.namedparam.*
import org.strangeforest.tcb.stats.service.*
import org.strangeforest.tcb.util.*

import com.google.common.base.*

import static org.strangeforest.tcb.dataload.LoaderUtil.*

class WikipediaPlayerDataLoader {

	final SqlPool sqlPool
	final PlayerService playerService

	static final int FETCH_THREAD_COUNT = 5

	static final String FETCH_PLAYERS_FOR_DATA_UPDATE_SQL = //language=SQL
		'SELECT player_id FROM player_v\n' +
		'WHERE (goat_points > 0 OR best_rank <= 500)\n' +
//		'AND (wikipedia IS NULL OR backhand IS NULL OR turned_pro IS NULL OR prize_money IS NULL OR web_site IS NULL)\n' +
		'ORDER BY goat_points DESC, best_rank'

	static final Map PARAM_CASTS = [
		hand: '::player_hand',
		backhand: '::player_backhand'
	]

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
		def updates = new ProgressTicker('.' as char, 1)
		def errors = new ProgressTicker('!' as char, 1)
		def newLine = ProgressTicker.newLineTicker().withPreAction { printf(' %1$.2f%%', 100.0 * (updates.ticks + errors.ticks) / playerIds.size()) }
		updates.withDownstreamTicker(newLine)
		errors.withDownstreamTicker(newLine)
		def pool = new ForkJoinPool(FETCH_THREAD_COUNT)
		try {
			pool.submit{
				playerIds.parallelStream().forEach { playerId ->
					try {
						def url = playerService.getPlayerWikipediaUrl(playerId)
						def playerData = findPlayerData(url)
						if (playerData) {
							updatePlayer(playerId, playerData)
							updates.tick()
						}
						else
							errors.tick()
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

		println "\n$updates.ticks players updated in $stopwatch, cannot update $errors.ticks players"
	}

	static def findPlayerData(String url) {
		def playerData = [:]
		playerData['wikipedia'] = url

		def doc = retriedGetDoc(url, false)
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
			if (plays.startsWith('right'))
				playerData['hand'] = 'R'
			else if (plays.startsWith('left'))
				playerData['hand'] = 'L'
			else if (plays.contains('right'))
				playerData['hand'] = 'R'
			else if (plays.contains('left'))
				playerData['hand'] = 'L'
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

		// DoD
		def died = findVCardField(vcard, 'died')
		if (died)
			playerData['dod'] = parseDate(died)

		playerData
	}

	static String findVCardField(vcard, String name) {
		def valueTr = vcard.select('tr').findResult { tr -> tr.select('th').text().toLowerCase().contains(name) ? tr : null }
		def text = valueTr?.select('td:first-of-type')?.text()
		text?.trim()?.replaceAll('\\s', ' ')
	}

	def updatePlayer(int playerId, Map params) {
		def paramsSql = params.keySet().collect{ param -> "$param = :$param" + (PARAM_CASTS[param] ?: '') }.join(', ')
		def paramMap = [playerId: playerId] << params
		sqlPool.withSql { sql ->
			sql.execute(paramMap, "UPDATE player SET $paramsSql WHERE player_id = :playerId")
		}
	}

	static Integer safeInteger(i) {
		try {
			i ? i.toInteger() : null
		}
		catch (ignored) {
			null
		}
	}

	static LocalDate parseDate(String d) {
		parseDate1(d) ?: parseDate2(d)
	}

	static LocalDate parseDate1(String d) {
		try {
			def sep1 = d.indexOf(' ')
			if (sep1 <= 0) return null
			def month = Month.valueOf(d.substring(0, sep1).toUpperCase())
			def sep2 = d.indexOf(', ', ++sep1)
			if (sep2 <= 0) return null
			def day = Integer.parseInt(d.substring(sep1, sep2))
			sep2 += 2
			def sep3 = d.indexOf(' ', sep2)
			if (sep3 <= 0) return null
			sep3 = d.indexOf('(', sep2)
			if (sep3 <= 0) return null
			def year = Integer.parseInt(d.substring(sep2, sep3))
			LocalDate.of(year, month, day)
		}
		catch (ignored) {
			null
		}
	}

	static LocalDate parseDate2(String d) {
		try {
			def sep1 = d.indexOf('(')
			if (sep1 <= 0) return null
			def sep2 = d.indexOf(')', ++sep1)
			if (sep2 <= 0) return null
			LocalDate.parse(d.substring(sep1, sep2))
		}
		catch (ignored) {
			null
		}
	}
}