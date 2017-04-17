package org.strangeforest.tcb.dataload

import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable

@EqualsAndHashCode @Sortable
class EventInfo {

	String extId
	String urlId
	boolean current

	EventInfo(String url) {
		extId = BaseATPWorldTourTournamentLoader.extract(url, '/', 5)
		urlId = BaseATPWorldTourTournamentLoader.extract(url, '/', 4)
		current = url.contains('current')
	}
}