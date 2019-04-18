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
			eventInfos.each { info ->
				atpTournamentLoader.loadTournament(season, info.urlId, info.extId, info.current)
			}
		}
		else
			println "No new completed tournaments found for season $season"
	}
}


static findCompletedEvents(int season) {
	def doc = retriedGetDoc("http://www.atptour.com/en/scores/results-archive?year=$season")
	Set eventInfos = new TreeSet()
	def DATE_FORMATTER = DateTimeFormatter.ofPattern('yyyy.MM.dd')
	doc.select('tr.tourney-result').each {result ->
		def url = result.select('td.tourney-details > a.button-border').attr('href')
		if (result.select('div.tourney-detail-winner > a')) {
			def date = LocalDate.parse(result.select('td.title-content > span.tourney-dates').text(), DATE_FORMATTER)
			eventInfos << new EventInfo(date, url)
		}
	}
	eventInfos
}