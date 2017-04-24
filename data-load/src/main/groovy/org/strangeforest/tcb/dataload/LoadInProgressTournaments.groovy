package org.strangeforest.tcb.dataload

import org.jsoup.*

import static org.strangeforest.tcb.dataload.BaseATPWorldTourTournamentLoader.*

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
		def atpInProgressTournamentLoader = new ATPWorldTourInProgressTournamentLoader(sql)
		def oldExtIds = atpInProgressTournamentLoader.findInProgressEventExtIds()
		println "Old in-progress tournaments: $oldExtIds"
		def eventInfos = findInProgressEvents('/en/scores/current')
		def newExtIds = eventInfos.collect { info -> info.extId }
		println "New in-progress tournaments: $newExtIds"
		eventInfos.each { info ->
			atpInProgressTournamentLoader.loadAndSimulateTournament(info.urlId, info.extId)
		}
		oldExtIds.removeAll(newExtIds)
		if (oldExtIds) {
			println "Removing finished in-progress tournaments: $oldExtIds"
			atpInProgressTournamentLoader.deleteInProgressEventExtIds(oldExtIds)
		}
	}
}

static findInProgressEvents(String url, boolean processUrls = true) {
	def doc = Jsoup.connect('http://www.atpworldtour.com' + url).timeout(TIMEOUT).get()
	def eventInfos = new TreeSet<>()
	eventInfos.addAll doc.select('div.arrow-next-tourney > div > a.tourney-title').collect { a ->
		def eventUrl = a.attr('href')
		if (processUrls)
			eventInfos.addAll findInProgressEvents(eventUrl, false)
		new EventInfo(eventUrl)
	}
	def eventUrl = doc.select('div.module-header > div.module-tabs > div.module-tab.current > span > a').attr('href')
	eventInfos << new EventInfo(eventUrl)
	if (processUrls)
		eventInfos.addAll findInProgressEvents(eventUrl, false)
	println eventInfos
	eventInfos
}