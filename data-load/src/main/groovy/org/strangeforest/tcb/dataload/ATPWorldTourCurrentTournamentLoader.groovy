package org.strangeforest.tcb.dataload

import java.time.*

import org.jsoup.*
import org.jsoup.select.*

import com.google.common.base.*
import groovy.sql.*

import static com.google.common.base.Strings.*
import static org.strangeforest.tcb.dataload.BaseXMLLoader.*

class ATPWorldTourCurrentTournamentLoader extends BaseATPWorldTourTournamentLoader {

	static final String LOAD_EVENT_SQL =
		'{call load_current_event(' +
			':ext_tournament_id, :date, :name, :tournament_level, :surface, :indoor, :draw_type, :draw_size' +
		')}'

	static final String LOAD_MATCH_SQL =
		'{call load_current_match(' +
			':ext_tournament_id, :match_num, :prev_match1_id, :prev_match2_id, :date, :surface, :indoor, :round, :best_of, ' +
			':player1_name, :player1_country_id, :player1_seed, :player1_entry, ' +
			':player2_name, :player2_country_id, :player2_seed, :player2_entry, ' +
			':winner, :score, :outcome' +
		')}'

	ATPWorldTourCurrentTournamentLoader(Sql sql) {
		super(sql)
	}

	def loadTournament(String urlId, extId, String level = null, String surface = null) {
		def url = tournamentUrl(urlId, extId)
		println "Fetching current tournament URL '$url'"
		def stopwatch = Stopwatch.createStarted()
		def doc = Jsoup.connect(url).timeout(TIMEOUT).get()
		def dates = doc.select('.tourney-dates').text()
		def atpLevel = extract(doc.select('.tourney-badge-wrapper > img:nth-child(1)').attr("src"), '_', 1)
		level = level ?: mapLevel(atpLevel)
		def name = getName(doc, level, LocalDate.now().getYear())
		def atpSurface = doc.select('td.tourney-details:nth-child(2) > div:nth-child(2) > div:nth-child(1) > span:nth-child(1)').text()
		surface = surface ?: mapSurface(atpSurface)
		def drawType = 'KO'
		def drawSize = doc.select('a.not-in-system:nth-child(1) > span:nth-child(1)').text()

		def matches = []
		def matchNum = 0
		def startDate = date extractStartDate(dates)

		def params = [:]
		params.ext_tournament_id = string extId
		params.date = startDate
		params.name = name
		params.tournament_level = level
		params.surface = surface
		params.indoor = surface == 'P' || name.toLowerCase().contains('indoor')
		params.draw_type = drawType
		params.draw_size = smallint drawSize
		sql.executeUpdate(params, LOAD_EVENT_SQL)

		def drawTable = doc.select('#scoresDrawTable')
		def entryRound = drawTable.select('thead > tr > th:nth-child(1)').text()
		println entryRound
		
		Elements entryMatches = drawTable.select('table.scores-draw-entry-box-table')
		entryMatches.each { entryMatch ->
			def name1 = emptyToNull(player(entryMatch.select('tbody > tr:nth-child(1) > td > a.scores-draw-entry-box-players-item').text()))
			def name2 = emptyToNull(player(entryMatch.select('tbody > tr:nth-child(2) > td > a.scores-draw-entry-box-players-item').text()))
			def seedEntry1 = extractSeedEntry entryMatch.select('tbody > tr:nth-child(1) > td:nth-child(2)').text()
			def seedEntry2 = extractSeedEntry entryMatch.select('tbody > tr:nth-child(2) > td:nth-child(2)').text()
			def isSeed1 = allDigits seedEntry1
			def isSeed2 = allDigits seedEntry2

			println "$seedEntry1 $name1 vs $seedEntry2 $name2"

			params = [:]
			params.ext_tournament_id = string extId
			params.match_num = smallint(++matchNum)
			params.date = startDate
			params.surface = surface
			params.indoor = surface == 'P' || name.toLowerCase().contains('indoor')
			params.round = entryRound
			params.best_of = smallint(mapBestOf(level))

			params.player1_name = name1
			params.player1_seed = isSeed1 ? smallint(seedEntry1) : null
			params.player1_entry = !isSeed1 ? mapEntry(string(seedEntry1)) : null

			params.player2_name = name2
			params.player2_seed = isSeed2 ? smallint(seedEntry2) : null
			params.player2_entry = !isSeed2 ? mapEntry(string(seedEntry2)) : null

			setScoreParams(params, '')

			matches << params
		}

		sql.withBatch(LOAD_MATCH_SQL) { ps ->
			matches.each { match ->
				ps.addBatch(match)
			}
		}
		sql.commit()
		println "\n$matches.size matches loaded in $stopwatch"
	}

	def setScoreParams(Map params, score) {
		def matchScore = MatchScoreParser.parse(score)
		params.winner = smallint(matchScore?.w_sets > matchScore?.l_sets ? 1 : (matchScore?.w_sets < matchScore?.l_sets ? 2 : null))
		params.score = string score
		params.outcome = matchScore?.outcome
	}


	static tournamentUrl(String urlId, extId) {
		"http://www.atpworldtour.com/en/scores/current/$urlId/$extId/draws"
	}
}
