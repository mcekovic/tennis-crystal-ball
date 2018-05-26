package org.strangeforest.tcb.dataload

import java.time.*

import groovy.json.JsonSlurper

loadTies(new SqlPool())

static loadTies(SqlPool sqlPool, Integer season = null) {
	sqlPool.withSql {sql ->
		def dcTieLoader = new DavisCupTieLoader(sql)
		season = season ?: LocalDate.now().year
		def ties = findCompletedTies(season)
		def seasonTies = dcTieLoader.findSeasonTies(season)
		ties.removeAll(seasonTies)
		if (ties) {
			println "New completed ties for season $season: $ties"
			ties.each { tie ->
				dcTieLoader.loadTie(season, tie)
			}
		}
		else
			println "No new completed ties found for season $season"
	}
}

static findCompletedTies(int season) {
	def calendar = new JsonSlurper().parse(new URL("https://media.itfdataservices.com/resultsbyyearlite/dc/en/$season"))

	def ties = []
	for (def event : calendar[0].Events) {
		println event
		for (def round : event.Rounds) {
			println '  ' + round
			for (def tie : round.Ties) {
				if (tie.PlayStatus == 'PC') {
					println '    ' + tie
					ties << tie.PublicTieId
				}
			}
		}
	}

	ties
}