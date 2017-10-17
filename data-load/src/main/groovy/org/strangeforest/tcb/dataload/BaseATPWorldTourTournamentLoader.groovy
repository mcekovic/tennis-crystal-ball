package org.strangeforest.tcb.dataload

import org.jsoup.nodes.*

import groovy.sql.*

import static org.strangeforest.tcb.dataload.BaseXMLLoader.*

abstract class BaseATPWorldTourTournamentLoader {

	protected final Sql sql

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
				return level == 'M' && season >= 1990 && !name.endsWith(' Masters') ? name + ' Masters' : name
		}
	}

	static mapLevel(String level) {
		switch (level) {
			case 'grandslam': return 'G'
			case 'finals-pos': return 'F'
			case '1000s': return 'M'
			case '500': return 'A'
			case '250':
			case 'atp':
			case 'atpwt':
			case 'challenger': return 'B'
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
		int pos = time.indexOf('Time:')
		if (pos >= 0)
			time = time.substring(pos + 5).trim()
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
		name.replace('-', ' ').replace('\'', '').replace('.', '').replace('ó', 'o').replace('á', 'a').replace('í', 'i').replace('ñ', 'n')
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
}
