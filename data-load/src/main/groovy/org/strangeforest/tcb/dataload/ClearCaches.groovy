package org.strangeforest.tcb.dataload

import org.jsoup.*

import static org.strangeforest.tcb.dataload.BaseATPWorldTourTournamentLoader.*

clearCaches()

static clearCaches() {
	def doc = Jsoup.connect('http://tennis-strangeforest.rhcloud.com/manage/clearCache').timeout(TIMEOUT).get()
	println doc.select('body').text()
}

