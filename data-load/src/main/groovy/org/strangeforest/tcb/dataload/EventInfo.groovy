package org.strangeforest.tcb.dataload

import java.time.LocalDate

import groovy.transform.*

@EqualsAndHashCode @ToString @Sortable
class EventInfo {

	LocalDate date
	String extId
	String urlId
	boolean current

	EventInfo(String url) {
		this(LocalDate.now(), url)
	}

	EventInfo(LocalDate date, String url) {
		this.date = date
		extId = BaseATPTourTournamentLoader.extract(url, '/', 5)
		urlId = BaseATPTourTournamentLoader.extract(url, '/', 4)
		current = url.contains('current')
	}
}