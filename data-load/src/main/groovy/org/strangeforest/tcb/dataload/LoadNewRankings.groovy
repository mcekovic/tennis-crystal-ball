package org.strangeforest.tcb.dataload

import java.time.*

import static java.time.DayOfWeek.*
import static java.time.format.DateTimeFormatter.*
import static java.time.temporal.TemporalAdjusters.*

loadRankings(new SqlPool())

static loadRankings(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
		def atpRankingsLoader = new ATPWorldTourRankingsLoader(sql)
		def date = LocalDate.now().with(previousOrSame(MONDAY)) format(ofPattern("yyyy-MM-dd"))
		println "Loading rankings for $date"
		atpRankingsLoader.load(date, 200)
	}
}