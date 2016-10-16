package org.strangeforest.tcb.dataload

import org.jsoup.*
import org.jsoup.select.*

class ATPWorldTourTournamentFetcher {

	private static final int TIMEOUT = 10 * 1000L

	static fetchATPTournament(int season, String urlId, def extId, String level = null) {
		def url = tournamentUrl(season, urlId, extId)
		println "Fetching URL '$url'"
		def doc = Jsoup.connect(url).timeout(TIMEOUT).get()

		def name = doc.select('span.tourney-title').text() ?: doc.select('td.title-content > a:nth-child(1)').text()
		println "Name: $name"
		def dates = doc.select('.tourney-dates').text()
		println "Dates: $dates"
		def atpLevel = extract(doc.select('.tourney-badge-wrapper > img:nth-child(1)').attr("src"), '_', 1)
		level = level ?: mapLevel(atpLevel)
		println "Level: $level"
		def surface = doc.select('td.tourney-details:nth-child(2) > div:nth-child(2) > div:nth-child(1) > span:nth-child(1)').text()
		println "Surface: $surface"
		def drawSize = doc.select('a.not-in-system:nth-child(1) > span:nth-child(1)').text()
		println "Draw: $drawSize"

		def matches = []
		def matchNum = 0
		def startDate = extractStartDate(dates)

		Elements rounds = doc.select('#scoresResultsContent div table')
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
				println "$round:"
				roundBody.select('tr').each { match ->
					def seeds = match.select('td.day-table-seed')
					def players = match.select('td.day-table-name a')
					def wSeedEntry = extractSeedEntry seeds.get(0).select('span').text()
					def wPlayer = players.get(0)
					def wName = player wPlayer.text()
					def wId = extract(wPlayer.attr('href'), '/', 4)
					def wIsSeed = allDigits wSeedEntry
					def lSeedEntry = extractSeedEntry seeds.get(1).select('span').text()
					def lPlayer = players.get(1)
					def lName = player lPlayer.text()
					def lId = extract(lPlayer.attr('href'), '/', 4)
					def lIsSeed = allDigits lSeedEntry
					def scoreElem = match.select('td.day-table-score a')
					def score = fitScore scoreElem.html().replace('<sup>', '(').replace('</sup>', ')')
					def bestOf = MatchScoreParser.parse(score).bestOf

					println "$wSeedEntry $wName [$wId] - $lSeedEntry $lName [$lId]: $score"

					matches << [
						'tourney_id': "$season-$extId",
						'match_num': String.valueOf(++matchNum),
						'tourney_name': name,
						'tourney_date': startDate,
						'tourney_level': level,
						'surface': surface,
						'draw_size': drawSize,
						'round': round,
						'score': score,
						'best_of': String.valueOf(bestOf ?: mapBestOf(level)),

						'winner_name': wName,
						'winner_seed': wIsSeed ? wSeedEntry : null,
						'winner_entry': !wIsSeed ? wSeedEntry : null,

						'loser_name': lName,
						'loser_seed': lIsSeed ? lSeedEntry : null,
						'loser_entry': !lIsSeed ? lSeedEntry : null,

//						'minutes': match['19'],
//
//						'w_ace': match['20'],
//						'w_df': match['21'],
//						'w_svpt': match['22'],
//						'w_1stIn': match['23'],
//						'w_1stWon': match['24'],
//						'w_2ndWon': match['25'],
//						'w_SvGms': match['26'],
//						'w_bpSaved': match['27'],
//						'w_bpFaced': match['28'],
//
//						'l_ace': match['29'],
//						'l_df': match['30'],
//						'l_svpt': match['31'],
//						'l_1stIn': match['32'],
//						'l_1stWon': match['33'],
//						'l_2ndWon': match['34'],
//						'l_SvGms': match['35'],
//						'l_bpSaved': match['36'],
//						'l_bpFaced': match['37']
					]
				}
			}
		}
		matches
	}

	static tournamentUrl(int season, String urlId, def extId) {
		"http://www.atpworldtour.com/en/scores/archive/$urlId/$extId/$season/results"
	}

	static extractStartDate(String dates) {
		int end = dates.indexOf('-')
		def startDate = end > 0 ? dates.substring(0, end) : dates
		startDate.trim().replace('.', '')
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
		level == 'G' ? 3 : 2
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
		if (seedEntry.startsWith('('))
			seedEntry = seedEntry.substring(1)
		if (seedEntry.endsWith(')'))
			seedEntry = seedEntry.substring(0, seedEntry.length() - 1)
		seedEntry
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
		name.replace('-', ' ')
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
