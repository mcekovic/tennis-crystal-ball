package org.strangeforest.tcb.dataload

import org.jsoup.*
import org.jsoup.select.*

import java.text.SimpleDateFormat

class ATPWorldTourTournamentFetcher {

	private static final int TIMEOUT = 10 * 1000L

	static fetchTournament(int season, String urlId, def extId, String level = null) {
		def url = tournamentUrl(season, urlId, extId)
		println "Fetching URL '$url'"
		def doc = Jsoup.connect(url).timeout(TIMEOUT).get()

		def name = doc.select('span.tourney-title').text() ?: doc.select('td.title-content > a:nth-child(1)').text()
		println "Name: $name"
		def dates = doc.select('.tourney-dates').text()
		println "Dates: $dates"
		def atpLevel = extract(doc.select('.tourney-badge-wrapper > img:nth-child(1)').attr("src"), '_', 1)
		println "Level: $level"
		def surface = doc.select('td.tourney-details:nth-child(2) > div:nth-child(2) > div:nth-child(1) > span:nth-child(1)').text()
		println "Surface: $surface"
		def drawSize = doc.select('a.not-in-system:nth-child(1) > span:nth-child(1)').text()
		println "Draw: $drawSize"

		def matches = []
		def matchNum = 0

		Elements rounds = doc.select('#scoresResultsContent div table')
		rounds.each {
			def roundHeads = it.select('thead')
			def roundBodies = it.select('tbody')
			def itHeads = roundHeads.iterator()
			def itBodies = roundBodies.iterator()
			while (itHeads.hasNext() && itBodies.hasNext()) {
				def roundHead = itHeads.next()
				def roundBody = itBodies.next()
				def round = roundHead.select('tr th').text()
				println "$round:"
				roundBody.select('tr').each { match ->
					def seeds = match.select('td.day-table-seed')
					def players = match.select('td.day-table-name a')
					def wSeed = seeds.get(0).select('span').text()
					def wPlayer = players.get(0)
					def wName = wPlayer.text()
					def wId = extract(wPlayer.attr('href'), '/', 4)
					def lSeed = seeds.get(1).select('span').text()
					def lPlayer = players.get(1)
					def lName = lPlayer.text()
					def lId = extract(lPlayer.attr('href'), '/', 4)
					def scoreElem = match.select('td.day-table-score a')
					def score = scoreElem.html().replace('<sup>', '(').replace('</sup>', ')')
					def statsUrl = scoreElem.attr('href')
					println "$wSeed $wName [$wId] - $lSeed $lName [$lId]: $score ($statsUrl)"

					matches << [
						'tourney_id': extId,
						'match_num': ++matchNum,
						'tourney_name': name,
						'tourney_date': extractStartDate(dates),
						'tourney_level': level ?: mapLevel(atpLevel),
						'surface': mapSurface(surface),
						'draw_size': drawSize,
						'round': round,
						'score': match['2'],
						'best_of': match['3'],

						'winner_name': match['5'],
						'winner_rank': match['6'],
						'winner_seed': match['7'],
						'winner_entry': match['8'],
						'winner_hand': match['9'],
						'winner_age': null,
						'winner_ioc': match['11'],

						'loser_name': match['12'],
						'loser_rank': match['13'],
						'loser_seed': match['14'],
						'loser_entry': match['15'],
						'loser_hand': match['16'],
						'loser_age': null,
						'loser_ioc': match['18'],

						'minutes': match['19'],

						'w_ace': match['20'],
						'w_df': match['21'],
						'w_svpt': match['22'],
						'w_1stIn': match['23'],
						'w_1stWon': match['24'],
						'w_2ndWon': match['25'],
						'w_SvGms': match['26'],
						'w_bpSaved': match['27'],
						'w_bpFaced': match['28'],

						'l_ace': match['29'],
						'l_df': match['30'],
						'l_svpt': match['31'],
						'l_1stIn': match['32'],
						'l_1stWon': match['33'],
						'l_2ndWon': match['34'],
						'l_SvGms': match['35'],
						'l_bpSaved': match['36'],
						'l_bpFaced': match['37']
					]
				}
			}
		}
		matches
	}

	static tournamentUrl(int season, String urlId, String extId) {
		"http://www.atpworldtour.com/en/scores/archive/$urlId/$extId/$season/results"
	}

	static extractStartDate(String dates) {
		int end = dates.indexOf('-')
		def startDate = end > 0 ? dates.substring(0, end) : dates
		Date.parse('yyyy.MM.dd', startDate.trim())
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
			case 'Grass': return 'G'
			case 'Clay': return 'C'
			case 'Carpet': return 'P'
			default: throw new IllegalArgumentException('Unknown surface: ' + surface)
		}
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
