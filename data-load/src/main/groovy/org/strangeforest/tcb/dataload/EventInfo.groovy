package org.strangeforest.tcb.dataload

import java.time.LocalDate

import groovy.transform.*

@EqualsAndHashCode(includes = "extId")
@ToString @Sortable(includes = "extId")
class EventInfo {

	LocalDate date
	String extId
	String urlId
	String name
	boolean current

	EventInfo(String url) {
		this(LocalDate.now(), url)
	}

	EventInfo(LocalDate date, String url, String name = null) {
		this.date = date
		extId = BaseATPTourTournamentLoader.extract(url, '/', 5)
		urlId = BaseATPTourTournamentLoader.extract(url, '/', 4)
		this.name = name
		current = url.contains('current')
	}
}