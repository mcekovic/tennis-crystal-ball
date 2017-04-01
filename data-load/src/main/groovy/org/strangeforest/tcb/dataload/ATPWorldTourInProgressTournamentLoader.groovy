package org.strangeforest.tcb.dataload

import com.google.common.base.*
import groovy.sql.*
import org.jsoup.*
import org.jsoup.select.*
import org.springframework.jdbc.core.namedparam.*
import org.strangeforest.tcb.stats.model.*
import org.strangeforest.tcb.stats.model.prediction.*
import org.strangeforest.tcb.stats.service.*

import java.time.*

import static com.google.common.base.Strings.*
import static org.strangeforest.tcb.dataload.BaseXMLLoader.*

class ATPWorldTourInProgressTournamentLoader extends BaseATPWorldTourTournamentLoader {

	static final String LOAD_EVENT_SQL =
		'{call load_in_progress_event(' +
			':ext_tournament_id, :date, :name, :tournament_level, :surface, :indoor, :draw_type, :draw_size' +
		')}'

	static final String LOAD_MATCH_SQL =
		'{call load_in_progress_match(' +
			':ext_tournament_id, :match_num, :prev_match_num1, :prev_match_num2, :date, :surface, :indoor, :round, :best_of, ' +
			':player1_name, :player1_country_id, :player1_seed, :player1_entry, ' +
			':player2_name, :player2_country_id, :player2_seed, :player2_entry, ' +
			':winner, :score, :outcome' +
		')}'

	static final String FETCH_MATCHES_SQL =
		'SELECT m.* FROM in_progress_match m\n' +
		'INNER JOIN in_progress_event e USING (in_progress_event_id)\n' +
		'INNER JOIN tournament_mapping tm USING (tournament_id)\n' +
		'WHERE tm.ext_tournament_id = ?\n' +
		'ORDER BY in_progress_event_id, round, match_num'

	static final String LOAD_PLAYER_RESULT_SQL =
		'{call load_player_in_progress_result(' +
			':in_progress_event_id, :player_id, :result, :probability' +
		')}'

	ATPWorldTourInProgressTournamentLoader(Sql sql) {
		super(sql)
	}

	def loadTournament(String urlId, extId, String level = null, String surface = null) {
		def stopwatch = Stopwatch.createStarted()
		def url = tournamentUrl(urlId, extId)
		println "Fetching in-progress tournament URL '$url'"
		def doc = Jsoup.connect(url).timeout(TIMEOUT).get()
		def dates = doc.select('.tourney-dates').text()
		def atpLevel = extract(doc.select('.tourney-badge-wrapper > img:nth-child(1)').attr("src"), '_', 1)
		level = level ?: mapLevel(atpLevel)
		def name = getName(doc, level, LocalDate.now().getYear())
		def atpSurface = doc.select('td.tourney-details:nth-child(2) > div:nth-child(2) > div:nth-child(1) > span:nth-child(1)').text()
		surface = surface ?: mapSurface(atpSurface)
		def indoor = surface == 'P' || name.toLowerCase().contains('indoor')
		def drawType = 'KO'
		def drawSize = doc.select('a.not-in-system:nth-child(1) > span:nth-child(1)').text()
		def bestOf = smallint(mapBestOf(level))

		def matches = [:]
		short matchNum = 0
		def startDate = date extractStartDate(dates)
		def seedEntries = [:]

		def params = [:]
		params.ext_tournament_id = string extId
		params.date = startDate
		params.name = name
		params.tournament_level = level
		params.surface = surface
		params.indoor = indoor
		params.draw_type = drawType
		params.draw_size = smallint drawSize
		sql.executeUpdate(params, LOAD_EVENT_SQL)

		def drawTable = doc.select('#scoresDrawTable')
		def rounds = drawTable.select('thead > tr > th').findAll().collect { round -> round.text() }
		def entryRound = rounds[0]
		println '\n' + entryRound

		Elements entryMatches = drawTable.select('table.scores-draw-entry-box-table')
		entryMatches.each { entryMatch ->
			matchNum += 1
			def name1 = emptyToNull(player(entryMatch.select('tbody > tr:nth-child(1) > td > a.scores-draw-entry-box-players-item').text()))
			def name2 = emptyToNull(player(entryMatch.select('tbody > tr:nth-child(2) > td > a.scores-draw-entry-box-players-item').text()))
			def seedEntry1 = extractSeedEntry entryMatch.select('tbody > tr:nth-child(1) > td:nth-child(2)').text()
			def seedEntry2 = extractSeedEntry entryMatch.select('tbody > tr:nth-child(2) > td:nth-child(2)').text()
			def isSeed1 = allDigits seedEntry1
			def isSeed2 = allDigits seedEntry2
			def seed1 = isSeed1 ? smallint(seedEntry1) : null
			def seed2 = isSeed2 ? smallint(seedEntry2) : null
			def entry1 = !isSeed1 ? mapEntry(string(seedEntry1)) : null
			def entry2 = !isSeed2 ? mapEntry(string(seedEntry2)) : null
			seedEntries[name1] = [seed: seed1, entry: entry1]
			seedEntries[name2] = [seed: seed2, entry: entry2]

			params = [:]
			params.ext_tournament_id = string extId
			params.match_num = matchNum
			params.date = startDate
			params.surface = surface
			params.indoor = indoor
			params.round = entryRound
			params.best_of = bestOf

			params.player1_name = name1
			params.player1_seed = seed1
			params.player1_entry = entry1

			params.player2_name = name2
			params.player2_seed = seed2
			params.player2_entry = entry2

			setScoreParams(params)

			matches[matchNum] = params

			println "$seedEntry1 $name1 vs $seedEntry2 $name2"
		}

		def drawRowSpan = 1
		short prevMatchNumOffset = 1
		rounds.findAll({round -> round != entryRound}).each { round ->
			println '\n' + round
			short matchNumOffset = matchNum + 1
			Elements roundPlayers = drawTable.select("tbody > tr > td[rowspan=$drawRowSpan")
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
						name1 = player roundPlayer.select('a.scores-draw-entry-box-players-item').text()
						def prevScoreElem = roundPlayer.select('a.scores-draw-entry-box-score')
						setScoreParams(matches[prevMatchNum1], prevScoreElem, name1)
					}
					else {
						matchNum += 1
						short prevMatchNum2 = prevMatchNumOffset + index
						def name2 = player roundPlayer.select('a.scores-draw-entry-box-players-item').text()
						def prevScoreElem = roundPlayer.select('a.scores-draw-entry-box-score')
						setScoreParams(matches[prevMatchNum2], prevScoreElem, name2)
						def seedEntry1 = seedEntries[name1]
						def seedEntry2 = seedEntries[name2]
						def seed1 = seedEntry1.seed
						def seed2 = seedEntry2.seed
						def entry1 = seedEntry1.entry
						def entry2 = seedEntry2.entry

						params = [:]
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

						println "${seed1 ?: ''}${entry1 ?: ''} $name1 vs ${seed2 ?: ''}${entry2 ?: ''} $name2"
					}
				}
			}
			drawRowSpan *= 2
			prevMatchNumOffset = matchNumOffset
		}

		sql.withBatch(LOAD_MATCH_SQL) { ps ->
			matches.values().each { match ->
				ps.addBatch(match)
			}
		}
		sql.commit()
		println "\n${matches.size()} matches loaded in $stopwatch"

		tournamentSimulation(extId, level)
	}

	def tournamentSimulation(extId, String level) {
		println '\nStarting tournament simulation'
		def stopwatch = Stopwatch.createStarted()
		def matches = sql.rows(FETCH_MATCHES_SQL, [string(extId)])
		MatchPredictionService predictor = new MatchPredictionService(new NamedParameterJdbcTemplate(SqlPool.dataSource()))
		def probabilities = [:]
		def results = []
		matches.each { match ->
			def inProgressEventId = match.in_progress_event_id
			def playerId1 = match.player1_id
			def playerId2 = match.player2_id
			def tournamentLevel = TournamentLevel.decode(level)
			def surface = Surface.decode(match.surface)
			def round = Round.decode(match.round)
			def bestOf = (Short) match.best_of
			def result = nextResult(round)
			Integer winner = match.winner

			MatchPrediction prediction
			if (playerId1 && playerId2 && !winner)
				prediction = predictor.predictMatch(playerId1, playerId2, match.date, surface, tournamentLevel, round, bestOf)

			if (playerId1) {
				def params1 = [:]
				params1.in_progress_event_id = inProgressEventId
				params1.player_id = playerId1
				params1.result = result
				def prevProbability1 = probabilities[playerId1] ?: 1.0
				def probability1 = winner == 1 ? 1.0 : (winner == 2 ? 0.0 : prediction.winProbability1)
				params1.probability = real prevProbability1 * probability1
				println params1
				results << params1
			}
			if (playerId2) {
				def params2 = [:]
				params2.in_progress_event_id = inProgressEventId
				params2.player_id = playerId2
				params2.result = result
				def prevProbability2 = probabilities[playerId2] ?: 1.0
				def probability2 = winner == 2 ? 1.0 : (winner == 1 ? 0.0 : prediction.winProbability2)
				params2.probability = real prevProbability2 * probability2
				println params2
				results << params2
			}
		}
		
		sql.withBatch(LOAD_PLAYER_RESULT_SQL) { ps ->
			results.each { result ->
				ps.addBatch(result)
			}
		}
		sql.commit()
		println "\nTournament simulation: ${matches.size()} result loaded in $stopwatch"
	}

	def nextResult(Round round) {
		switch (round) {
			case Round.F: return 'W'
			case Round.SF: return 'F'
			case Round.QF: return 'SF'
			case Round.R16: return 'QF'
			case Round.R32: return 'R16'
			case Round.R64: return 'R32'
			case Round.R128: return 'R64'
			default: return round.name()
		}
	}

	static setScoreParams(Map params, scoreElem = null, winnerName = null) {
		if (scoreElem) {
			def score = fitScore scoreElem.html().replace('<sup>', '(').replace('</sup>', ')')
			def matchScore = MatchScoreParser.parse(score)
			if (winnerName == params.player1_name)
				params.winner = (short)1
			else if (winnerName == params.player2_name)
				params.winner = (short)2
			params.score = string score
			params.outcome = matchScore?.outcome
		}
	}

	static tournamentUrl(String urlId, extId) {
		"http://www.atpworldtour.com/en/scores/current/$urlId/$extId/draws"
	}
}
