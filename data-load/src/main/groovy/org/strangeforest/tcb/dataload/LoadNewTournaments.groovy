package org.strangeforest.tcb.dataload

import java.time.*

import static org.strangeforest.tcb.dataload.LoaderUtil.*

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool, Integer season = null) {
	sqlPool.withSql {sql ->
		def atpTournamentLoader = new ATPWorldTourTournamentLoader(sql)
		season = season ?: LocalDate.now().year
		def eventInfos = findCompletedEvents(season)
		def seasonExtIds = atpTournamentLoader.findSeasonEventExtIds(season)
		eventInfos.removeAll { info -> info.extId in seasonExtIds }
		def newExtIds = eventInfos.collect { info -> info.extId }
		println "New completed tournaments for season $season: $newExtIds"
		eventInfos.each { info ->
			atpTournamentLoader.loadTournament(season, info.urlId, info.extId, info.current)
		}
	}
}

static findCompletedEvents(int season) {
	def doc = retriedGetDoc("http://www.atpworldtour.com/en/scores/results-archive?year=$season")
	Set eventInfos = new TreeSet()
	doc.select('tr.tourney-result').each {result ->
		def url = result.select('td.tourney-details > a.button-border').attr('href')
		if (result.select('div.tourney-detail-winner > a'))
			eventInfos << new EventInfo(url)
	}
	eventInfos
}