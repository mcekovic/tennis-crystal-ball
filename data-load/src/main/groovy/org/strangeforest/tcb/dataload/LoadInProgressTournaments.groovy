package org.strangeforest.tcb.dataload

import static org.strangeforest.tcb.dataload.LoaderUtil.*

System.exit(loadTournaments(new SqlPool()) ? 0 : 80)

static loadTournaments(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
		def atpInProgressTournamentLoader = new ATPWorldTourInProgressTournamentLoader(sql)
		def oldExtIds = atpInProgressTournamentLoader.findInProgressEventExtIds()
		println "Old in-progress tournaments: $oldExtIds"
		def result = findInProgressEvents()
		if (result.errorAndEmpty)
			return false
		def eventInfos = result.items
		def newExtIds = eventInfos.collect { info -> info.extId }
		println "New in-progress tournaments: $newExtIds"
		def changed = false
		eventInfos.each { info ->
			changed |= atpInProgressTournamentLoader.loadAndForecastTournament(info.urlId, info.extId, null, null, null, true)
		}
		oldExtIds.removeAll(newExtIds)
		if (oldExtIds) {
			println "Removing finished in-progress tournaments: $oldExtIds"
			atpInProgressTournamentLoader.completeInProgressEventExtIds(oldExtIds)
			changed = true
		}
		changed
	}
}

static findInProgressEvents() {
	CrawlingResult<EventInfo> result = CrawlingResult.empty()
	def crawler = new CascadingCrawler('/en/scores/current')
	crawler.visit {
		url -> result.add findInProgressEvents(url, crawler)
	}
	result.items.removeIf { info -> !info.extId }
	result
}

static CrawlingResult<EventInfo> findInProgressEvents(String url, CascadingCrawler crawler) {
	def fullUrl = 'http://www.atpworldtour.com' + url
	def eventInfos = new TreeSet<>()
	try {
		def doc = retriedGetDoc(fullUrl)
		eventInfos.addAll doc.select('div.arrow-next-tourney > div > a.tourney-title').collect { a ->
			def eventUrl = a.attr('href').replace('overview', 'draws').replace('tournaments', 'scores/current')
			crawler.addUrl eventUrl
			new EventInfo(eventUrl)
		}
		def eventUrl = doc.select('div.module-header > div.module-tabs > div.module-tab.current > span > a').attr('href')
		crawler.addUrl eventUrl
		eventInfos << new EventInfo(eventUrl)
	}
	catch (Exception ex) {
		System.err.println 'Error fetching URL: ' + fullUrl
		ex.printStackTrace()
		CrawlingResult.error(eventInfos)
	}
	CrawlingResult.ok(eventInfos)
}

class CascadingCrawler {

	Set<String> visitedUrls
	Set<String> urls

	CascadingCrawler(String... urls) {
		visitedUrls = new HashSet<>()
		this.urls = new HashSet<>(Arrays.asList(urls))
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

class CrawlingResult<T> {

	Set<T> items
	boolean error

	static <T> CrawlingResult<T> empty() {
		new CrawlingResult(new TreeSet<>(), false)
	}

	static <T> CrawlingResult<T> ok(Set<T> items) {
		new CrawlingResult(items, false)
	}

	static <T> CrawlingResult<T> error(Set<T> items) {
		new CrawlingResult(items, true)
	}

	private CrawlingResult(Set<T> items, boolean error) {
		this.items = items
		this.error = error
	}

	def add(CrawlingResult<T> result) {
		items.addAll(result.items)
		error = error || result.error
	}

	def getErrorAndEmpty() {
		error && !items
	}
}