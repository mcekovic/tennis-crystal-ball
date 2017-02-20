package org.strangeforest.tcb.dataload

import com.google.common.base.*
import groovy.json.*
import groovy.sql.*
import org.jsoup.*
import org.jsoup.nodes.*
import org.jsoup.select.*

import java.util.concurrent.atomic.*

import static org.strangeforest.tcb.dataload.XMLMatchLoader.*

class ATPWorldTourTournamentLoader {

	private final Sql sql

	private static final int TIMEOUT = 30 * 1000L
	private static final int PROGRESS_LINE_WRAP = 100

	ATPWorldTourTournamentLoader(Sql sql) {
		this.sql = sql
	}

	def loadTournament(int season, String urlId, extId, String level = null, boolean current = false) {
		def url = tournamentUrl(current, season, urlId, extId)
		println "Fetching tournament URL '$url'"
		def stopwatch = Stopwatch.createStarted()
		def doc = Jsoup.connect(url).timeout(TIMEOUT).get()
		def dates = doc.select('.tourney-dates').text()
		def atpLevel = extract(doc.select('.tourney-badge-wrapper > img:nth-child(1)').attr("src"), '_', 1)
		level = level ?: mapLevel(atpLevel)
		def name = getName(doc, level, season)
		def surface = doc.select('td.tourney-details:nth-child(2) > div:nth-child(2) > div:nth-child(1) > span:nth-child(1)').text()
		def drawType = 'KO'
		def drawSize = doc.select('a.not-in-system:nth-child(1) > span:nth-child(1)').text()

		def matches = []
		def matchNum = 0
		def startDate = date extractStartDate(dates)

		Elements rounds = doc.select('#scoresResultsContent div table')
		if (rounds.toString().contains('Round Robin'))
			drawType = 'RR'
		rounds.each {
			def roundHeads = it.select('thead')
			def roundBodies = it.select('tbody')
			def itHeads = roundHeads.iterator()
			def itBodies = roundBodies.iterator()
			while (itHeads.hasNext() && itBodies.hasNext()) {
				def roundHead = itHeads.next()
				def roundBody = itBodies.next()
				def round = mapRound roundHead.select('tr th').text()
				if (!round) continue
				roundBody.select('tr').each { match ->
					def seeds = match.select('td.day-table-seed')
					def players = match.select('td.day-table-name a')
					def wSeedEntry = extractSeedEntry seeds.get(0).select('span').text()
					def wPlayer = players.get(0)
					def wName = player wPlayer.text()
//					def wId = extract(wPlayer.attr('href'), '/', 4)
					def wIsSeed = allDigits wSeedEntry
					def lSeedEntry = extractSeedEntry seeds.get(1).select('span').text()
					def lPlayer = players.get(1)
					def lName = player lPlayer.text()
//					def lId = extract(lPlayer.attr('href'), '/', 4)
					def lIsSeed = allDigits lSeedEntry
					def scoreElem = match.select('td.day-table-score a')
					def score = fitScore scoreElem.html().replace('<sup>', '(').replace('</sup>', ')')
					def bestOf = MatchScoreParser.parse(score).bestOf

					def params = [:]
					params.ext_tournament_id = string extId
					params.season = smallint season
					params.tournament_date = startDate
					params.tournament_name = name
					params.event_name = name
					params.tournament_level = level
					params.surface = mapSurface surface
					params.indoor = false
					params.draw_type = drawType
					params.draw_size = smallint drawSize

					params.match_num = smallint(++matchNum)
					params.date = startDate
					params.round = round
					params.best_of = smallint bestOf ?: mapBestOf(level)

					params.winner_name = wName
					params.winner_seed = wIsSeed ? smallint(wSeedEntry) : null
					params.winner_entry = !wIsSeed ? mapEntry(string(wSeedEntry)) : null

					params.loser_name = lName
					params.loser_seed = lIsSeed ? smallint(lSeedEntry) : null
					params.loser_entry = !lIsSeed ? mapEntry(string(lSeedEntry)) : null

					setScoreParams(params, score, sql.connection)
					params.statsUrl = matchStatsUrl(scoreElem.attr('href'))

					matches << params
				}
			}
		}

		AtomicInteger rows = new AtomicInteger()
		matches.parallelStream().forEach { params ->
			def statsUrl = params.statsUrl
			if (statsUrl) {
				def statsDoc = Jsoup.connect(statsUrl).timeout(TIMEOUT).get()
				def json = statsDoc.select('#matchStatsData').html()
				def matchStats = new JsonSlurper().parseText(json)[0]
				def wStats = matchStats.playerStats
				def lStats = matchStats.opponentStats

				params.minutes = minutes wStats.Time
				this.setStatsParams(params, wStats, 'w_')
				this.setStatsParams(params, lStats, 'l_')
				print '.'
			}
			if (rows.incrementAndGet() % PROGRESS_LINE_WRAP == 0)
				println()
		}

		sql.withBatch(LOAD_SQL) { ps ->
			matches.each { match ->
				ps.addBatch(match)
			}
		}
		sql.commit()
		println "\n$matches.size matches loaded in $stopwatch"
	}

	def setScoreParams(Map params, score, conn) {
		def matchScore = MatchScoreParser.parse(score)
		params.score = string score
		params.outcome = matchScore.outcome
		params.w_sets = matchScore?.w_sets
		params.l_sets = matchScore?.l_sets
		params.w_games = matchScore?.w_games
		params.l_games = matchScore?.l_games
		params.w_set_games = matchScore ? shortArray(conn, matchScore.w_set_games) : null
		params.l_set_games = matchScore ? shortArray(conn, matchScore.l_set_games) : null
		params.w_set_tb_pt = matchScore ? shortArray(conn, matchScore.w_set_tb_pt) : null
		params.l_set_tb_pt = matchScore ? shortArray(conn, matchScore.l_set_tb_pt) : null
	}

	def setStatsParams(Map params, stats, prefix) {
		params[prefix + 'ace'] = smallint stats?.Aces
		params[prefix + 'df'] = smallint stats?.DoubleFaults
		params[prefix + 'sv_pt'] = smallint stats?.FirstServeDivisor
		params[prefix + '1st_in'] = smallint stats?.FirstServeDividend
		params[prefix + '1st_won'] = smallint stats?.FirstServePointsWonDividend
		params[prefix + '2nd_won'] = smallint stats?.SecondServePointsWonDividend
		params[prefix + 'sv_gms'] = smallint stats?.ServiceGamesPlayed
		params[prefix + 'bp_sv'] = smallint stats?.BreakPointsSavedDividend
		params[prefix + 'bp_fc'] = smallint stats?.BreakPointsSavedDivisor
	}

	static tournamentUrl(boolean current, int season, String urlId, extId) {
		def type = current ? 'current' : 'archive'
		"http://www.atpworldtour.com/en/scores/$type/$urlId/$extId/$season/results"
	}

	static matchStatsUrl(String url) {
		url ? "http://www.atpworldtour.com" + url : null
	}

	static extractStartDate(String dates) {
		int end = dates.indexOf('-')
		def startDate = end > 0 ? dates.substring(0, end) : dates
		startDate.trim().replace('.', '-')
	}

	static getName(Document doc, String level, int season) {
		switch (level) {
			case 'G': return doc.select('span.tourney-title').text() ?: doc.select('td.title-content > a:nth-child(1)').text()
			case 'F': return 'Tour Finals'
			default:
				def location = doc.select('td.title-content > span:nth-child(2)').text()
				def pos = location.indexOf(',')
				def name = pos > 0 ? location.substring(0, pos) : location
				return level == 'M' && season >= 1990 && !name.endsWith(' Masters') ? name + ' Masters' : name
		}
	}

	static mapLevel(String level) {
		switch (level) {
			case 'grandslam': return 'G'
			case 'finals-pos': return 'F'
			case '1000s': return 'M'
			case '500': return 'A'
			case '250': return 'B'
			default: throw new IllegalArgumentException('Unknown tournament level: ' + level)
		}
	}

	static mapSurface(String surface) {
		switch (surface) {
			case 'Hard': return 'H'
			case 'Clay': return 'C'
			case 'Grass': return 'G'
			case 'Carpet': return 'P'
			default: return null
		}
	}

	static mapRound(String round) {
		switch (round) {
			case 'Finals': return 'F'
			case 'Semi-Finals': return 'SF'
			case 'Quarter-Finals': return 'QF'
			case 'Round of 16': return 'R16'
			case 'Round of 32': return 'R32'
			case 'Round of 64': return 'R64'
			case 'Round of 128': return 'R128'
			case 'Round Robin': return 'RR'
			case 'Olympic Bronze': return 'BR'
			default: return null
		}
	}

	static mapBestOf(String level) {
		level == 'G' ? 5 : 3
	}

	static mapEntry(String entry) {
		if (entry) {
			if (entry == 'S')
				return 'SE'
		}
		entry
	}

	static fitScore(String score) {
		def setScores = []
		for (String setScore : score.split('\\s+'))
			setScores << fitSetScore(setScore)
		setScores.join(' ')
	}

	static fitSetScore(String setScore) {
		int tb = setScore.indexOf('(')
		String gamesScore = tb >= 0 ? setScore.substring(0, tb) : setScore
		if (allDigits(gamesScore)) {
			int len = gamesScore.length()
			int half = (len + 1) / 2
			String fitSetScore = gamesScore.substring(0, half) + '-' + gamesScore.substring(half)
			tb >= 0 ? fitSetScore + setScore.substring(tb) : fitSetScore
		}
		else
			setScore
	}

	static extractSeedEntry(String seedEntry) {
		def openingBrace = seedEntry.indexOf('(')
		if (openingBrace >= 0)
			seedEntry = seedEntry.substring(openingBrace + 1)
		def closingBrace = seedEntry.indexOf(')')
		if (closingBrace >= 0)
			seedEntry = seedEntry.substring(0, closingBrace)
		seedEntry
	}

	static minutes(String time) {
		time ? smallint(60 * Integer.parseInt(time.substring(0, 2)) + Integer.parseInt(time.substring(3, 5))) : null
	}

	static allDigits(String s) {
		if (!s) return false
		for (char c : s.toCharArray()) {
			if (!c.isDigit())
				return false
		}
		true
	}

	static player(String name) {
		name.replace('-', ' ').replace('\'', '')
	}

	static extract(String s, String delimiter, int occurrence) {
		int start = 0
		for (int i in 1..occurrence) {
			start = s.indexOf(delimiter, start)
			if (start < 0) return ''
			start++
		}
		int end = s.indexOf(delimiter, start)
		return end > 0 ? s.substring(start, end) : s.substring(start)
	}
}
