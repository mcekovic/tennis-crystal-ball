package org.strangeforest.tcb.dataload

import java.util.concurrent.*

import org.jsoup.nodes.*
import org.strangeforest.tcb.stats.model.forecast.*
import org.strangeforest.tcb.util.*

import groovy.sql.*

import static org.strangeforest.tcb.dataload.BaseXMLLoader.*
import static org.strangeforest.tcb.dataload.LoaderUtil.*

abstract class BaseATPTourTournamentLoader {

	protected final Sql sql

	private static final int FETCH_STATS_THREAD_COUNT = 1
	private static final long FETCH_STATS_PAUSE_MIN = 5000L
	private static final long FETCH_STATS_PAUSE_MAX = 10000L

	BaseATPTourTournamentLoader(Sql sql) {
		this.sql = sql
	}

	static extractStartDate(String dates) {
		int end = dates.indexOf('-')
		def startDate = end > 0 ? dates.substring(0, end) : dates
		startDate.trim().replace('.', '-')
	}

	static getName(Document doc, String level, int season) {
		switch (level) {
			case 'G': return doc.select('span.tourney-title').text() ?: doc.select('td.title-content > a.tourney-title').text()
			case 'F': return 'Tour Finals'
			default:
				def name = doc.select('td.title-content > a.tourney-title').text()
				def location = doc.select('td.title-content > span.tourney-location').text()
				def pos = location.indexOf(',')
				def city = pos > 0 ? location.substring(0, pos) : location
				name = name ?: city

				def pos2 = name.length() - 2
				if (pos2 > 0 && name.charAt(pos2) == '-')
					name = name.substring(0, pos2) + ' ' + name.substring(pos2 + 1)
				switch (level) {
					case 'M':
						if (name.startsWith('ATP Masters 1000 '))
							name = name.substring(17)
						if (season >= 1990 && !name.endsWith(' Masters'))
							name += ' Masters'
						return name
					case 'H':
						if (name.equals('Next Gen ATP Finals'))
							return 'Next Gen Finals'
						else
							return name
					default: return name
				}
		}
	}

	static mapLevel(String level, String urlId) {
		switch (level) {
			case 'grandslam': return 'G'
			case 'finals':
			case 'finals-pos': return 'F'
			case '1000':
			case '1000s': return 'M'
			case '500': return 'A'
			case '250':
			case 'challenger': return 'B'
			case 'atp':
			case 'atpwt': return urlId.contains('finals') ? 'F' : 'B'
			default:
				System.err.println "Unknown tournament level: $level"
				return 'H'
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

	static mapIndoor(String surface, String name, int season) {
		if (name.toLowerCase().contains('indoor'))
			return true
		switch (surface) {
			case 'P': return true
			case 'H': return (season >= 2017 && (
					name.startsWith('Montpellier') ||
					name.startsWith('Sofia') ||
					name.startsWith('Memphis') ||
					name.startsWith('Rotterdam') ||
					name.startsWith('Marseille') ||
					name.startsWith('Metz') ||
					name.startsWith('St. Petersburg') ||
					name.startsWith('Antwerp') ||
					name.startsWith('Moscow') ||
					name.startsWith('Stockholm') ||
					name.startsWith('Basel') ||
					name.startsWith('Vienna') ||
					name.startsWith('Paris Masters') ||
					name.startsWith('Next Gen Finals') ||
					name.startsWith('Tour Finals')
				)) ||
				(season >= 2018 && (
					(name.startsWith('New York') && !(name == "New York Masters")) ||
					(name.startsWith('Tokyo') && season == 2018)
				)) ||
				(season >= 2019 && (
					name.startsWith('Davis Cup Finals')
				)) ||
				(season >= 2020 && (
					name.startsWith('Cologne') ||
					name.startsWith('Nur-Sultan')
				))
			case 'C': return (season >= 2018 && (
					name.startsWith('Sao Paulo')
				))
			default: return false
		}
	}

	static mapDrawType(String level) {
		switch (level) {
			case 'F': return 'RR'
			default: return 'KO'
		}
	}

	static mapRound(String round, List<String> rounds = null) {
		switch (round) {
			case 'Final':
			case 'Finals': return 'F'
			case 'Olympic Bronze':
			case '3rd/4th Place Match': return 'BR'
			case 'Semifinals':
			case 'Semi-Finals': return 'SF'
			case 'Quarterfinals':
			case 'Quarter-Finals': return 'QF'
			case 'Round of 16': return 'R16'
			case 'Round of 32': return 'R32'
			case 'Round of 64': return 'R64'
			case 'Round of 128': return 'R128'
			case 'Round Robin': return 'RR'
			case '1st Rd':
			case '2nd Rd':
			case '3rd Rd':
			case '4th Rd':
				int pos = rounds.indexOf(round)
				for (int i = pos + 1; i < rounds.size(); i++) {
					int j = KOResult.values().findIndexOf { v -> v.name() == rounds[i]}
					if (j >= 0)
						return KOResult.values()[j].offset(pos - i).name()
				}
				break
			default: return null
		}
		round
	}

	static mapBestOf(String level) {
		level == 'G' ? 5 : 3
	}

	static mapEntry(String entry) {
		if (entry) {
			switch (entry) {
				case 'W': return 'WC'
				case 'L': return 'LL'
				case 'S': return 'SE'
				case 'Alt': return 'AL'
			}
		}
		entry
	}

	static fitScore(String score) {
		if (!score)
			return 'W/O'
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
			if (len == 0)
				throw new IllegalArgumentException("Invalid set score: $setScore")
			if (len == 1) {
				def oldGamesScore = gamesScore
				int leftGames = Integer.parseInt(gamesScore)
				int rightGames = Math.max(6, leftGames + 2)
				gamesScore += rightGames
				len = gamesScore.length()
				System.err.println("WARN: Invalid set games score: $oldGamesScore, approximating to: $gamesScore")
			}
			int half = (len + 1) / 2
			def leftGames = gamesScore.substring(0, half)
			def rightGames = gamesScore.substring(half)
			if (len % 2 == 1 && Math.abs(Integer.parseInt(leftGames) - Integer.parseInt(rightGames)) > 2) {
				--half
				leftGames = gamesScore.substring(0, half)
				rightGames = gamesScore.substring(half)
			}
			String fitSetScore = leftGames + '-' + rightGames
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
		int hPos = time.indexOf('Time:')
		if (hPos >= 0)
			time = time.substring(hPos + 5).trim()
		int mPos = time.indexOf(':')
		if (mPos < 0)
			return null
		int hours = (int)Float.parseFloat(time.substring(0, mPos))
		mPos++
		int sPos = time.indexOf(':', mPos)
		int mins
		if (sPos >= 0)
			mins = (int)Float.parseFloat(time.substring(mPos, sPos))
		else {
			mins = hours
			hours = 0
		}
		time ? smallint(60 * hours + mins) : null
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
		name.replace('-', ' ').replace('\'', '').replace('.', '').replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ñ', 'n').replace('ó', 'o').replace('ú', 'u')
	}

	static isUnknown(String name) {
		name?.toUpperCase()?.contains('UNKNOWN')
	}

	static isQualifier(String name) {
		name?.toUpperCase()?.contains('QUALIFIER')
	}

	static isBye(String name) {
		name?.toUpperCase()?.contains('BYE')
	}

	static extract(String s, String delimiter, int occurrence) {
		int start = 0
		for (int i in 1..occurrence) {
			start = s.indexOf(delimiter, start)
			if (start < 0) return ''
			start++
		}
		int end = s.indexOf(delimiter, start)
		end > 0 ? s.substring(start, end) : s.substring(start)
	}

	static extract(String s, String from, String to) {
		int start = from ? s.indexOf(from) : 0
		int end = to ? s.indexOf(to) : s.length()
		start >= 0 && end > 0 ? s.substring(start + from.length(), end) : null
	}


	// Statistics

	static loadStats(matches, int season, extId, String prefix1, String prefix2, String nameProperty1, String nameProperty2) {
		def progress = new ProgressTicker('.' as char, 1).withDownstreamTicker(ProgressTicker.newLineTicker())
		def pool = new ForkJoinPool(FETCH_STATS_THREAD_COUNT)
		def rnd = new Random()
		try {
			pool.submit {
				matches.parallelStream().forEach { match ->
					try {
						def statsUrl = match.statsUrl
						def forcedUrl = false
						// ATP Web site strips in-match stats links for GS tournaments for one month (probably some legal issue with ITF), but stats are there, just links are stripped!
						if (!statsUrl && season > 1991 && match.tournament_level == 'G') {
							statsUrl = matchStatsUrl(season, extId, String.format('%03d', match.match_num))
							forcedUrl = true
						}
						if (statsUrl) {
							randomCrawl(rnd)
							def statsDoc = retriedGetDoc(statsUrl)
							if (forcedUrl) {
								def title = statsDoc.select('div.modal-scores-header h3.section-title').text()
								if (title) {
									match = findMatch(matches, title, nameProperty1, nameProperty2)
									if (!match)
										println "\nMatch '$title' cannot be found"
								}
							}
							if (match && statsDoc)
								setMatchStatsParams(match, statsDoc, prefix1, prefix2, nameProperty1, nameProperty2, progress)
						}
					}
					catch (Exception ex) {
						ex.printStackTrace()
					}
				}
			}
		}
		finally {
			pool.shutdown()
		}
		pool.awaitTermination(1L, TimeUnit.HOURS)
		if (progress.ticks > 0)
			println()
	}

	static matchStatsUrl(String url) {
		url ? "https://www.atptour.com" + url : null
	}

	static matchStatsUrl(int season, extId, String matchNum) {
		"https://www.atptour.com/en/scores/match-stats/archive/$season/$extId/MS$matchNum"
	}

	static setMatchStatsParams(Map match, statsDoc, String prefix1, String prefix2, String nameProperty1, String nameProperty2, ProgressTicker progress) {
		match.minutes = minutes statsDoc.select('#completedScoreBox table.scores-table tr.match-info-row td.time').text()
		def leftName = statsExtractName(statsDoc.select('#modalScoresContentContainer div.match-stats-player-left'))
		def rightName = statsExtractName(statsDoc.select('#modalScoresContentContainer div.match-stats-player-right'))
		if (leftName.toLowerCase() == match[nameProperty2].toLowerCase() && rightName.toLowerCase() == match[nameProperty1].toLowerCase()) {
			def temp = prefix1; prefix1 = prefix2; prefix2 = temp
		}
		def matchStats = statsDoc.select('#completedMatchStats > table.match-stats-table')
		if (matchStats) {
			setATPStatsParams(match, matchStats, prefix1, prefix2)
			progress.tick()
		}
	}

	static statsExtractName(playerElem) {
		player(playerElem.select('span.first-name').text()) + ' ' + player(playerElem.select('span.last-name').text())
	}

	static setATPStatsParams(Map params, stats, String prefix1, String prefix2) {
		setATPStatParams(params, stats, 'ace', 'Aces', prefix1, prefix2)
		setATPStatParams(params, stats, 'df', 'Double Faults', prefix1, prefix2)
		setATPStatParamsUpDown(params, stats, '1st_in', 'sv_pt', '1st Serve', prefix1, prefix2)
		setATPStatParamsUpDown(params, stats, '1st_won', null, '1st Serve Points Won', prefix1, prefix2)
		setATPStatParamsUpDown(params, stats, '2nd_won', null, '2nd Serve Points Won', prefix1, prefix2)
		setATPStatParams(params, stats, 'sv_gms', 'Service Games Played', prefix1, prefix2)
		setATPStatParamsUpDown(params, stats, 'bp_sv', 'bp_fc', 'Break Points Saved', prefix1, prefix2)
	}

	static setATPStatParams(Map params, stats, String name, String title, String prefix1, String prefix2) {
		def stat = stats.select("tr.match-stats-row:has(td.match-stats-label:containsOwn(${title}))")
		params[prefix1 + name] = smallint stat.select('td.match-stats-number-left').text()
		params[prefix2 + name] = smallint stat.select('td.match-stats-number-right').text()
	}

	static setATPStatParamsUpDown(Map params, stats, String nameUp, String nameDown, String title, String prefix1, String prefix2) {
		def stat = stats.select("tr.match-stats-row:has(td.match-stats-label:containsOwn(${title}))")
		String wText = stat.select('td.match-stats-number-left').text()
		String lText = stat.select('td.match-stats-number-right').text()
		if (nameUp) {
			params[prefix1 + nameUp] = smallint statUp(wText)
			params[prefix2 + nameUp] = smallint statUp(lText)
		}
		if (nameDown) {
			params[prefix1 + nameDown] = smallint statDown(wText)
			params[prefix2 + nameDown] = smallint statDown(lText)
		}
	}

	static String statUp(String s) {
		extract(s, '(', '/')
	}

	static String statDown(String s) {
		extract(s, '/', ')')
	}

	static Map findMatch(matches, String title, String nameProperty1, String nameProperty2) {
		title = player title.toLowerCase()
		matches.find { match ->
			title.contains(match[nameProperty1].toLowerCase()) && title.contains(match[nameProperty2].toLowerCase())
		}
	}

	static randomCrawl(Random rnd) {
		randomPause(rnd)
		if (rnd.nextInt(10) == 0) {
			retriedGetDoc('https://www.atptour.com')
			print '*'
			randomPause(rnd)
		}
	}

	static randomPause(Random rnd) {
		Thread.sleep(FETCH_STATS_PAUSE_MIN + rnd.nextInt(FETCH_STATS_PAUSE_MAX - FETCH_STATS_PAUSE_MIN as int))
	}
}
