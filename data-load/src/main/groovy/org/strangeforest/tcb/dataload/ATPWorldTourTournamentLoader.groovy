package org.strangeforest.tcb.dataload

import java.util.concurrent.atomic.*

import org.jsoup.*
import org.jsoup.select.*

import com.google.common.base.*
import groovy.json.*
import groovy.sql.*

import static org.strangeforest.tcb.dataload.LoaderUtil.*
import static org.strangeforest.tcb.dataload.XMLMatchLoader.*

class ATPWorldTourTournamentLoader extends BaseATPWorldTourTournamentLoader {

	static final String SELECT_SEASON_EVENT_EXT_IDS_SQL = //language=SQL
		'SELECT ext_tournament_id FROM tournament_event\n' +
		'INNER JOIN tournament_mapping USING (tournament_id)\n' +
		'WHERE season = :season\n' +
		'ORDER BY ext_tournament_id'


	ATPWorldTourTournamentLoader(Sql sql) {
		super(sql)
	}

	def loadTournament(int season, String urlId, extId, boolean current = false, String level = null, String surface = null, Collection<String> skipRounds = Collections.emptySet()) {
		def url = tournamentUrl(current, season, urlId, extId)
		println "Fetching tournament URL '$url'"
		def stopwatch = Stopwatch.createStarted()
		def doc = Jsoup.connect(url).timeout(TIMEOUT).get()
		def dates = doc.select('.tourney-dates').text()
		def atpLevel = extract(doc.select('.tourney-badge-wrapper > img:nth-child(1)').attr("src"), '_', 1)
		level = level ?: mapLevel(atpLevel)
		def name = getName(doc, level, season)
		def atpSurface = doc.select('td.tourney-details:nth-child(2) > div:nth-child(2) > div:nth-child(1) > span:nth-child(1)').text()
		surface = surface ?: mapSurface(atpSurface)
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
				if (!round || skipRounds.contains(round)) continue
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
					def matchScore = MatchScoreParser.parse(score)
					def bestOf = matchScore.bestOf

					def params = [:]
					params.ext_tournament_id = string extId
					params.season = smallint season
					params.tournament_date = startDate
					params.tournament_name = name
					params.event_name = name
					params.tournament_level = level
					params.surface = surface
					params.indoor = surface == 'P' || name.toLowerCase().contains('indoor')
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

					params.score = matchScore?.toString()
					setScoreParams(params, matchScore)
					params.statsUrl = matchStatsUrl(scoreElem.attr('href'))

					if ((isUnknownOrQualifier(wName) || isUnknownOrQualifier(lName)))
						return
					
					matches << params
				}
			}
		}

		AtomicInteger rows = new AtomicInteger()
		matches.parallelStream().forEach { params ->
			def statsUrl = params.statsUrl
			if (statsUrl) {
				retry(5, { th -> th instanceof HttpStatusException }, {
					def statsDoc = Jsoup.connect(statsUrl).timeout(TIMEOUT).get()
					def json = statsDoc.select('#matchStatsData').html()
					def matchStats = new JsonSlurper().parseText(json)[0]
					def wStats = matchStats.playerStats
					def lStats = matchStats.opponentStats

					params.minutes = minutes wStats.Time
					setATPStatsParams(params, wStats, 'w_')
					setATPStatsParams(params, lStats, 'l_')
					print '.'
				})
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

	def setScoreParams(Map params, MatchScore matchScore) {
		params.outcome = matchScore.outcome
		if (matchScore) {
			params.w_sets = matchScore.w_sets
			params.l_sets = matchScore.l_sets
			params.w_games = matchScore.w_games
			params.l_games = matchScore.l_games
			def conn = sql.connection
			params.w_set_games = shortArray(conn, matchScore.w_set_games)
			params.l_set_games = shortArray(conn, matchScore.l_set_games)
			params.w_set_tb_pt = shortArray(conn, matchScore.w_set_tb_pt)
			params.l_set_tb_pt = shortArray(conn, matchScore.l_set_tb_pt)
		}
	}

	static setATPStatsParams(Map params, stats, String prefix) {
		if (!stats)
			return
		params[prefix + 'ace'] = smallint stats.Aces
		params[prefix + 'df'] = smallint stats.DoubleFaults
		params[prefix + 'sv_pt'] = smallint stats.FirstServeDivisor
		params[prefix + '1st_in'] = smallint stats.FirstServeDividend
		params[prefix + '1st_won'] = smallint stats.FirstServePointsWonDividend
		params[prefix + '2nd_won'] = smallint stats.SecondServePointsWonDividend
		params[prefix + 'sv_gms'] = smallint stats.ServiceGamesPlayed
		params[prefix + 'bp_sv'] = smallint stats.BreakPointsSavedDividend
		params[prefix + 'bp_fc'] = smallint stats.BreakPointsSavedDivisor
	}

	static tournamentUrl(boolean current, int season, String urlId, extId) {
		def type = current ? 'current' : 'archive'
		def seasonUrl = current ? '' : '/' + season
		"http://www.atpworldtour.com/en/scores/$type/$urlId/$extId$seasonUrl/results"
	}

	static matchStatsUrl(String url) {
		url ? "http://www.atpworldtour.com" + url : null
	}

	static isUnknownOrQualifier(String name) {
		isUnknown(name) || isQualifier(name)
	}


	// Maintenance

	def findSeasonEventExtIds(int season) {
		sql.rows([season: season], SELECT_SEASON_EVENT_EXT_IDS_SQL).collect { row -> row.ext_tournament_id }
	}
}
