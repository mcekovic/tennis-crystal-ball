package org.strangeforest.tcb.dataload

import java.util.concurrent.*
import java.util.concurrent.atomic.*

import org.jsoup.nodes.*

import groovy.sql.*

import static org.strangeforest.tcb.dataload.BaseXMLLoader.*
import static org.strangeforest.tcb.dataload.LoaderUtil.*

abstract class BaseATPWorldTourTournamentLoader {

	protected final Sql sql

	protected static final int FETCH_THREAD_COUNT = 5
	protected static final int PROGRESS_LINE_WRAP = 100

	BaseATPWorldTourTournamentLoader(Sql sql) {
		this.sql = sql
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
				if (level == 'M') {
					if (name.startsWith('Montreal'))
						name = 'Canada' + name.substring(8)
					else if (name.startsWith('Toronto'))
						name = 'Canada' + name.substring(7)
					if (season >= 1990 && !name.endsWith(' Masters'))
						name += ' Masters'
				}
				return name
		}
	}

	static mapLevel(String level, String urlId) {
		switch (level) {
			case 'grandslam': return 'G'
			case 'finals-pos': return 'F'
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
				name.startsWith('Tour Finals') ||
				name.startsWith('Milan'))
				||
				(season >= 2018 && name.startsWith('Tokyo'))
			)
			default: return false
		}
	}

	static mapDrawType(String level) {
		switch (level) {
			case 'F': return 'RR'
			default: return 'KO'
		}
	}

	static mapRound(String round) {
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
			default: return null
		}
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
		name.replace('-', ' ').replace('\'', '').replace('.', '').replace('ó', 'o').replace('á', 'a').replace('í', 'i').replace('ñ', 'n').replace('ú', 'u')
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
		int start = s.indexOf(from)
		int end = s.indexOf(to)
		start >= 0 && end > 0 ? s.substring(start + 1, end) : null
	}


	// Statistics

	static loadStats(matches, String prefix1, String prefix2) {
		AtomicInteger rows = new AtomicInteger()
		ForkJoinPool pool = new ForkJoinPool(FETCH_THREAD_COUNT)
		try {
			pool.submit {
				matches.parallelStream().forEach { params ->
					def statsUrl = params.statsUrl
					if (statsUrl) {
						def statsDoc = retriedGetDoc(statsUrl)
						params.minutes = minutes statsDoc.select('#completedScoreBox table.scores-table tr.match-info-row td.time').text()
						def matchStats = statsDoc.select('#completedMatchStats > table.match-stats-table')
						if (matchStats) {
							setATPStatsParams(params, matchStats, prefix1, prefix2)
							print '.'
						}
					}
					if (rows.incrementAndGet() % PROGRESS_LINE_WRAP == 0)
						println()
				}
			}.get()
		}
		finally {
			pool.shutdown()
		}
		pool.awaitTermination(1L, TimeUnit.HOURS)
		if (rows.get() > 0)
			println()
	}

	static reloadStats(matches, int season, extId, String prefix1, String prefix2) {
		AtomicInteger rows = new AtomicInteger()
		ForkJoinPool pool = new ForkJoinPool(FETCH_THREAD_COUNT)
		def matchNums = 1..matches.size()
		try {
			pool.submit {
				matchNums.parallelStream().forEach { matchNum ->
					def statsUrl = matchStatsUrl(season, extId, String.format('%03d', matchNum))
					def statsDoc = retriedGetDoc(statsUrl)
					def match = findMatch(matches, statsDoc.select('div.modal-scores-header h3.section-title').text())
					if (match) {
						match.minutes = minutes statsDoc.select('#completedScoreBox table.scores-table tr.match-info-row td.time').text()
						def matchStats = statsDoc.select('#completedMatchStats > table.match-stats-table')
						if (matchStats) {
							setATPStatsParams(match, matchStats, prefix1, prefix2)
							print '.'
						}
						if (rows.incrementAndGet() % PROGRESS_LINE_WRAP == 0)
							println()
					}
				}
			}
		}
		finally {
			pool.shutdown()
		}
		pool.awaitTermination(1L, TimeUnit.HOURS)
		if (rows.get() > 0)
			println()
	}

	static matchStatsUrl(String url) {
		url ? "http://www.atpworldtour.com" + url : null
	}

	static matchStatsUrl(int season, extId, String matchNum) {
		"http://www.atpworldtour.com/en/scores/$season/$extId/MS$matchNum/match-stats"
	}

	static setATPStatsParams(Map params, stats, String prefix1, String prefix2) {
		if (params.winner == 2) {
			def prefix = prefix1; prefix1 = prefix2; prefix2 = prefix
		}
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

	static findMatch(matches, String title) {
		title = title.toLowerCase()
		matches.find { match ->
			title.contains(match.winner_name.toLowerCase()) && title.contains(match.loser_name.toLowerCase())
		}
	}
}
