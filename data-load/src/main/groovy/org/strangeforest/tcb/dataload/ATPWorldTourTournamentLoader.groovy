package org.strangeforest.tcb.dataload

import java.util.concurrent.atomic.*

import org.jsoup.select.*

import com.google.common.base.*
import groovy.sql.*

import static org.strangeforest.tcb.dataload.LoaderUtil.*
import static org.strangeforest.tcb.dataload.SqlPool.*
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

	def loadTournament(int season, String urlId, extId, boolean current = false, String level = null, String surface = null, Collection<String> skipRounds = Collections.emptySet(), String name = null, overrideExtId = null) {
		def url = tournamentUrl(current, season, urlId, extId)
		println "Fetching tournament URL '$url'"
		def stopwatch = Stopwatch.createStarted()
		def doc = retriedGetDoc(url)
		def dates = doc.select('.tourney-dates').text()
		def atpLevel = extract(doc.select('.tourney-badge-wrapper > img:nth-child(1)').attr("src"), '_', 1)
		level = level ?: mapLevel(atpLevel)
		name = name ?: getName(doc, level, season)
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
				if (!round || round in skipRounds) continue
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
					params.ext_tournament_id = string(overrideExtId ?: extId)
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
				def statsDoc = retriedGetDoc(statsUrl)
				params.minutes = minutes statsDoc.select('#completedScoreBox table.scores-table tr.match-info-row td.time').text()
				def matchStats = statsDoc.select('#completedMatchStats > table.match-stats-table')
				if (matchStats) {
					setATPStatsParams(params, matchStats)
					print '.'
				}
			}
			if (rows.incrementAndGet() % PROGRESS_LINE_WRAP == 0)
				println()
		}

		withTx sql, { Sql s ->
			s.withBatch(LOAD_SQL) { ps ->
				matches.each { match ->
					ps.addBatch(match)
				}
			}
		}
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

	static setATPStatsParams(Map params, stats) {
		setATPStatParams(params, stats, 'ace', 'Aces')
		setATPStatParams(params, stats, 'df', 'Double Faults')
		setATPStatParamsUpDown(params, stats, '1st_in', 'sv_pt', '1st Serve')
		setATPStatParamsUpDown(params, stats, '1st_won', null, '1st Serve Points Won')
		setATPStatParamsUpDown(params, stats, '2nd_won', null, '2nd Serve Points Won')
		setATPStatParams(params, stats, 'sv_gms', 'Service Games Played')
		setATPStatParamsUpDown(params, stats, 'bp_sv', 'bp_fc', 'Break Points Saved')
	}

	static setATPStatParams(Map params, stats, String name, String title) {
		def stat = stats.select("tr.match-stats-row:has(td.match-stats-label:containsOwn(${title}))")
		params['w_' + name] = smallint stat.select('td.match-stats-number-left').text()
		params['l_' + name] = smallint stat.select('td.match-stats-number-right').text()
	}

	static setATPStatParamsUpDown(Map params, stats, String nameUp, String nameDown, String title) {
		def stat = stats.select("tr.match-stats-row:has(td.match-stats-label:containsOwn(${title}))")
		String wText = stat.select('td.match-stats-number-left').text()
		String lText = stat.select('td.match-stats-number-right').text()
		if (nameUp) {
			params['w_' + nameUp] = smallint statUp(wText)
			params['l_' + nameUp] = smallint statUp(lText)
		}
		if (nameDown) {
			params['w_' + nameDown] = smallint statDown(wText)
			params['l_' + nameDown] = smallint statDown(lText)
		}
	}

	static String statUp(String s) {
		extract(s, '(', '/')
	}

	static String statDown(String s) {
		extract(s, '/', ')')
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
