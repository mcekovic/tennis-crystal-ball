package org.strangeforest.tcb.dataload

import static java.util.Arrays.asList
import static org.strangeforest.tcb.dataload.LoaderUtil.retriedGetDoc

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
	def eventInfos = new TreeSet<>()
	def crawler = new CascadingCrawler('/en/scores/current')
	crawler.visit { url -> eventInfos.addAll findInProgressEvents(url, crawler) }
	eventInfos.removeIf { info -> !info.extId }
	eventInfos
}

static findInProgressEvents(String url, CascadingCrawler crawler) {
	def doc = retriedGetDoc('http://www.atpworldtour.com' + url)
	def eventInfos = new TreeSet<>()
	eventInfos.addAll doc.select('div.arrow-next-tourney > div > a.tourney-title').collect { a ->
		def eventUrl = a.attr('href')
		crawler.addUrl eventUrl
		new EventInfo(eventUrl)
	}
	def eventUrl = doc.select('div.module-header > div.module-tabs > div.module-tab.current > span > a').attr('href')
	crawler.addUrl eventUrl
	eventInfos << new EventInfo(eventUrl)
	eventInfos
}

class CascadingCrawler {

	Set<String> visitedUrls
	Set<String> urls

	CascadingCrawler(String... urls) {
		visitedUrls = new HashSet<>()
		this.urls = new HashSet<>(asList(urls))
	}

	def visit(Closure c) {
		while (!urls.empty) {
			for (String url : new ArrayList<>(urls)) {
				c(url)
				visited url
			}
		}
	}

	def isVisited(String url) {
		visitedUrls.contains(url)
	}

	def visited(String url) {
		visitedUrls.add url
		urls.remove url
	}

	def addUrl(String url) {
		if (!isVisited(url))
			urls.add url
	}
}