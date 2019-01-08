package org.strangeforest.tcb.dataload

import org.jsoup.*

clearCaches()

static clearCaches() {
	def doc = Jsoup.connect('http://localhost/actuator/clearcache').timeout(10 * 1000).post()
	println doc.select('body').text()
}

