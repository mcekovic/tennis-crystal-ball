package org.strangeforest.tcb.dataload

import java.time.*
import java.time.format.DateTimeFormatter

import static org.strangeforest.tcb.dataload.LoaderUtil.*

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool, Integer season = null) {
	sqlPool.withSql {sql ->
		def atpTournamentLoader = new ATPTourTournamentLoader(sql)
		season = season ?: LocalDate.now().year
		def eventInfos = findCompletedEvents(season)
		def seasonExtIds = atpTournamentLoader.findSeasonEventExtIds(season)
		eventInfos.removeAll { info -> info.extId in seasonExtIds }
		if (eventInfos) {
			def newExtIds = eventInfos.collect { info -> info.extId }
			println "New completed tournaments for season $season: $newExtIds"
			for (info in eventInfos)
				atpTournamentLoader.loadTournament(season, info.urlId, info.extId, info.current)
		}
		else
			println "No new completed tournaments found for season $season"
	}
}

static listMissingTournaments(SqlPool sqlPool, Integer season = null) {
	sqlPool.withSql {sql ->
		def atpTournamentLoader = new ATPTourTournamentLoader(sql)
		season = season ?: LocalDate.now().year
		def eventInfos = findCompletedEvents(season)
		def seasonExtIds = atpTournamentLoader.findSeasonEventExtIds(season)
		eventInfos.removeAll { info -> info.extId in seasonExtIds }
		if (eventInfos) {
			println "\nMissing tournaments for season $season:"
			for (info in eventInfos) {
				def url = ATPTourTournamentLoader.tournamentUrl(false, season, info.urlId, info.extId, false)
				println "$info.date $info.name ($url)"
			}
		}
		else
			println "\nNo missing tournaments found for season $season"
	}
}

static findCompletedEvents(int season) {
	def doc = retriedGetDoc("https://www.atptour.com/en/scores/results-archive?year=$season")
	Set eventInfos = new TreeSet()
	def DATE_FORMATTER = DateTimeFormatter.ofPattern('yyyy.MM.dd')
	doc.select('tr.tourney-result').each {result ->
		def url = result.select('td.tourney-details > a.button-border').attr('href')
		def winner = result.select('div.tourney-detail-winner')
		if (winner && winner.text().contains('SGL') && winner.select('a')) {
			def date = LocalDate.parse(result.select('td.title-content > span.tourney-dates').text(), DATE_FORMATTER)
			def name = result.select('td.title-content > span.tourney-title').text()?.trim()
			eventInfos << new EventInfo(date, url, name)
		}
	}
	eventInfos
}