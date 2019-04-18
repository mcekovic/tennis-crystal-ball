package org.strangeforest.tcb.dataload


import org.jsoup.select.*

import com.google.common.base.*
import groovy.sql.*

import static org.strangeforest.tcb.dataload.BaseXMLLoader.*
import static org.strangeforest.tcb.dataload.LoaderUtil.*
import static org.strangeforest.tcb.dataload.SqlPool.*
import static org.strangeforest.tcb.dataload.XMLMatchLoader.*

class ATPTourTournamentLoader extends BaseATPTourTournamentLoader {

	static final String SELECT_SEASON_EVENT_EXT_IDS_SQL = //language=SQL
		'SELECT ext_tournament_id FROM tournament_event\n' +
		'INNER JOIN tournament_mapping USING (tournament_id)\n' +
		'WHERE season = :season\n' +
		'ORDER BY ext_tournament_id'


	ATPTourTournamentLoader(Sql sql) {
		super(sql)
	}

	def loadTournament(int season, String urlId, extId, boolean current = false, String level = null, String surface = null, Collection<String> skipRounds = Collections.emptySet(), String name = null, overrideExtId = null, boolean scrapeDraws = false, boolean forceReloadStats = false) {
		def url = tournamentUrl(current, season, urlId, extId, scrapeDraws)
		println "Fetching tournament URL '$url'"
		def stopwatch = Stopwatch.createStarted()
		def doc = retriedGetDoc(url)
		List<Object> matches = scrapeDraws
			? scrapeDraws(doc, level, urlId, name, season, surface, skipRounds, overrideExtId, extId)
			: scrapeResults(doc, level, urlId, name, season, surface, skipRounds, overrideExtId, extId)

		loadStats(matches, 'w_', 'l_', 'winner_name', 'loser_name')
		if (forceReloadStats)
			reloadStats(matches, season, extId, 'w_', 'l_', 'winner_name', 'loser_name')

		withTx sql, { Sql s ->
			s.withBatch(LOAD_SQL) { ps ->
				matches.each { match ->
					ps.addBatch(match)
				}
			}
		}
		println "$matches.size matches loaded in $stopwatch"
	}

	def scrapeResults(doc, String level, String urlId, String name, int season, String surface, Collection<String> skipRounds, overrideExtId, extId) {
		def dates = doc.select('.tourney-dates').text()
		def atpLevel = extract(extract(doc.select('.tourney-badge-wrapper > img:nth-child(1)').attr("src"), '_', 1), '', '.')
		level = level ?: mapLevel(atpLevel, urlId)
		name = name ?: getName(doc, level, season)
		def atpSurface = doc.select('td.tourney-details:nth-child(2) > div:nth-child(2) > div:nth-child(1) > span:nth-child(1)').text()
		surface = surface ?: mapSurface(atpSurface)
		def indoor = mapIndoor(surface, name, season)
		def drawType = mapDrawType(level)
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
					def bestOf = smallint matchScore.bestOf ?: mapBestOf(level)

					def params = [:]
					params.ext_tournament_id = string(overrideExtId ?: extId)
					params.season = smallint season
					params.tournament_date = startDate
					params.tournament_name = name
					params.event_name = name
					params.tournament_level = level
					params.surface = surface
					params.indoor = indoor
					params.draw_type = drawType
					params.draw_size = smallint drawSize

					params.match_num = smallint(++matchNum)
					params.date = startDate
					params.round = round
					params.best_of = bestOf

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
		matches
	}

	def scrapeDraws(doc, String level, String urlId, String name, int season, String surface, Collection<String> skipRounds, overrideExtId, extId, verbose) {
		def dates = doc.select('.tourney-dates').text()
		def atpLevel = extract(extract(doc.select('.tourney-badge-wrapper > img:nth-child(1)').attr("src"), '_', 1), '', '.')
		level = level ?: mapLevel(atpLevel, urlId)
		name = name ?: getName(doc, level, season)
		def atpSurface = doc.select('td.tourney-details:nth-child(2) > div:nth-child(2) > div:nth-child(1) > span:nth-child(1)').text()
		surface = surface ?: mapSurface(atpSurface)
		def indoor = mapIndoor(surface, name, season)
		def drawType = mapDrawType(level)
		def drawSize = doc.select('a.not-in-system:nth-child(1) > span:nth-child(1)').text()

		def matches = [:]
		short matchNum = 0
		def startDate = date extractStartDate(dates)
		def seedEntries = [:]

		def drawTable = doc.select('#scoresDrawTable')
		def rounds = drawTable.select('thead > tr > th').findAll().collect { round -> round.text() }
		rounds = rounds.collect { r -> mapRound(r, rounds)}
		def entryRound = rounds[0]
		if (verbose) {
			println "Rounds $rounds"
			println '\n' + entryRound
		}

		// Processing entry round
		Elements entryMatches = drawTable.select('table.scores-draw-entry-box-table')
		entryMatches.each { entryMatch ->
			matchNum += 1
			def name1 = extractEntryPlayer(entryMatch, 1)
			def name2 = extractEntryPlayer(entryMatch, 2)
			def seedEntry1 = extractSeedEntry entryMatch.select('tbody > tr:nth-child(1) > td:nth-child(2)').text()
			def seedEntry2 = extractSeedEntry entryMatch.select('tbody > tr:nth-child(2) > td:nth-child(2)').text()
			if (isQualifier(name1)) {
				name1 = null
				if (!seedEntry1)
					seedEntry1 = 'Q'
			}
			if (isQualifier(name2)) {
				name2 = null
				if (!seedEntry2)
					seedEntry2 = 'Q'
			}
			def isSeed1 = allDigits seedEntry1
			def isSeed2 = allDigits seedEntry2
			def seed1 = isSeed1 ? smallint(seedEntry1) : null
			def seed2 = isSeed2 ? smallint(seedEntry2) : null
			def entry1 = !isSeed1 ? mapEntry(string(seedEntry1)) : null
			def entry2 = !isSeed2 ? mapEntry(string(seedEntry2)) : null
			seedEntries[name1] = [seed: seed1, entry: entry1]
			seedEntries[name2] = [seed: seed2, entry: entry2]

			def bestOf = smallint(mapBestOf(level))

			def params = [:]
			params.ext_tournament_id = string(overrideExtId ?: extId)
			params.season = smallint season
			params.tournament_date = startDate
			params.tournament_name = name
			params.event_name = name
			params.tournament_level = level
			params.surface = surface
			params.indoor = indoor
			params.draw_type = drawType
			params.draw_size = smallint drawSize

			params.match_num = matchNum
			params.date = startDate
			params.round = entryRound
			params.best_of = bestOf

			//TODO
			params.player1_name = name1
			params.player1_seed = seed1
			params.player1_entry = entry1

			params.player2_name = name2
			params.player2_seed = seed2
			params.player2_entry = entry2

			setScoreParams(params)

			matches[matchNum] = params

			if (verbose)
				println "$seedEntry1 $name1 vs $seedEntry2 $name2"
		}

		// Processing other rounds
		def drawRowSpan = 1
		short prevMatchNumOffset = 1
		rounds.findAll { round -> round != entryRound }.each { round ->
			if (verbose)
				println '\n' + round
			short matchNumOffset = matchNum + 1
			Elements roundPlayers = drawTable.select("tbody > tr > td[rowspan=$drawRowSpan]")
			if (!roundPlayers.select('div.scores-draw-entry-box').find { e -> e.text() })
				return
			if (round == 'Winner' && roundPlayers.size() == 2) // Workaround ATP website bug
				round = 'F'
			if (round == 'Winner') {
				def winner = roundPlayers.get(0)
				def winnerName = player winner.select('a.scores-draw-entry-box-players-item').text()
				def finalScoreElem = winner.select('a.scores-draw-entry-box-score')
				setScoreParams(matches[prevMatchNumOffset], finalScoreElem, winnerName)
			}
			else {
				def name1
				short prevMatchNum1
				roundPlayers.eachWithIndex { roundPlayer, index ->
					if (index % 2 == 0) {
						prevMatchNum1 = prevMatchNumOffset + index
						name1 = extractPlayer(roundPlayer)
						def prevScoreElem = roundPlayer.select('a.scores-draw-entry-box-score')
						setScoreParams(matches[prevMatchNum1], prevScoreElem, name1)
					}
					else {
						matchNum += 1
						short prevMatchNum2 = prevMatchNumOffset + index
						def name2 = extractPlayer(roundPlayer)
						def prevScoreElem = roundPlayer.select('a.scores-draw-entry-box-score')
						setScoreParams(matches[prevMatchNum2], prevScoreElem, name2)
						def seedEntry1 = seedEntries[name1]
						def seedEntry2 = seedEntries[name2]
						def seed1 = seedEntry1?.seed
						def seed2 = seedEntry2?.seed
						def entry1 = seedEntry1?.entry
						def entry2 = seedEntry2?.entry

						def params = [:]
						params.ext_tournament_id = string extId
						params.match_num = matchNum
						params.prev_match_num1 = prevMatchNum1
						params.prev_match_num2 = prevMatchNum2
						params.date = startDate
						params.surface = surface
						params.indoor = indoor
						params.round = round
						params.best_of = bestOf

						params.player1_name = name1
						params.player1_seed = seed1
						params.player1_entry = entry1

						params.player2_name = name2
						params.player2_seed = seed2
						params.player2_entry = entry2

						setScoreParams(params)

						matches[matchNum] = params

						if (verbose)
							println "${seed1 ?: ''}${entry1 ?: ''} $name1 vs ${seed2 ?: ''}${entry2 ?: ''} $name2"
					}
				}
			}
			drawRowSpan *= 2
			prevMatchNumOffset = matchNumOffset
		}
	}

	def setScoreParams(Map params, MatchScore matchScore) {
		params.outcome = matchScore.outcome
		if (matchScore) {
			params.w_sets = matchScore.w_sets
			params.l_sets = matchScore.l_sets
			params.w_games = matchScore.w_games
			params.l_games = matchScore.l_games
			params.w_tbs = matchScore.w_tbs
			params.l_tbs = matchScore.l_tbs
			def conn = sql.connection
			params.w_set_games = shortArray(conn, matchScore.w_set_games)
			params.l_set_games = shortArray(conn, matchScore.l_set_games)
			params.w_set_tb_pt = shortArray(conn, matchScore.w_set_tb_pt)
			params.l_set_tb_pt = shortArray(conn, matchScore.l_set_tb_pt)
			params.w_set_tbs = shortArray(conn, matchScore.w_set_tbs)
			params.l_set_tbs = shortArray(conn, matchScore.l_set_tbs)
		}
	}

	static tournamentUrl(boolean current, int season, String urlId, extId, boolean scrapeDraws) {
		def type = current ? 'current' : 'archive'
		def seasonUrl = current ? '' : '/' + season
		def resultsUrl = scrapeDraws ? '/draws' : '/results'
		"http://www.atptour.com/en/scores/$type/$urlId/$extId$seasonUrl/$resultsUrl"
	}

	static isUnknownOrQualifier(String name) {
		isUnknown(name) || isQualifier(name)
	}


	// Maintenance

	def findSeasonEventExtIds(int season) {
		sql.rows([season: season], SELECT_SEASON_EVENT_EXT_IDS_SQL).collect { row -> row.ext_tournament_id }
	}
}
