package org.strangeforest.tcb.dataload

import org.jsoup.*

import groovy.transform.*

import static org.strangeforest.tcb.dataload.BaseATPWorldTourTournamentLoader.*

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
		def atpInProgressTournamentLoader = new ATPWorldTourInProgressTournamentLoader(sql)
		def oldExtIds = atpInProgressTournamentLoader.findInProgressEventExtIds()
		println "Old in-progress tournaments: $oldExtIds"
		def eventInfos = findInProgressEvents()
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

static findInProgressEvents() {
	def doc = Jsoup.connect('http://www.atpworldtour.com/en/scores/current').timeout(TIMEOUT).get()
	def eventInfos = new TreeSet(doc.select('div.arrow-next-tourney > div > a.tourney-title').collect { a ->
		def url = a.attr('href')
		new InProgressEventInfo(url)
	})
	def url = doc.select('div.module-header > div.module-tabs > div.module-tab.current > span > a').attr('href')
	eventInfos << new InProgressEventInfo(url)
	eventInfos
}

@EqualsAndHashCode @Sortable
class InProgressEventInfo {

	String extId
	String urlId

	InProgressEventInfo(String url) {
		extId = extract(url, '/', 5)
		urlId = extract(url, '/', 4)
	}
}