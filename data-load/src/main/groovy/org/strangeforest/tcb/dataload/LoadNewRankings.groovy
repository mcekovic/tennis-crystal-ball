package org.strangeforest.tcb.dataload

import org.springframework.jdbc.core.namedparam.*
import org.strangeforest.tcb.stats.model.*
import org.strangeforest.tcb.stats.service.*

import java.time.*

import static java.time.DayOfWeek.*
import static java.time.format.DateTimeFormatter.*
import static java.time.temporal.TemporalAdjusters.*

loadRankings(new SqlPool())

static loadRankings(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
		def atpRankingsLoader = new ATPWorldTourRankingsLoader(sql)
		def rankingService = new RankingsService(new NamedParameterJdbcTemplate(SqlPool.dataSource()))
		def lastDate = rankingService.getCurrentRankingDate(RankType.POINTS)
		def currentDate = LocalDate.now().with(previousOrSame(MONDAY))
		for (def date = lastDate.with(next(MONDAY)); date <= currentDate; date = date.with(next(MONDAY))) {
			def formattedDate = date.format(ofPattern("yyyy-MM-dd"))
			println "Loading rankings for $formattedDate"
			atpRankingsLoader.load(formattedDate, 200)
		}
	}
}