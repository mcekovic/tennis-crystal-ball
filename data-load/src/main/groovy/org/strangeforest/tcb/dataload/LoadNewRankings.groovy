package org.strangeforest.tcb.dataload

import java.sql.*
import java.time.*

import org.springframework.jdbc.core.namedparam.*
import org.strangeforest.tcb.stats.model.core.*
import org.strangeforest.tcb.stats.service.*

import static java.time.DayOfWeek.*
import static java.time.format.DateTimeFormatter.*
import static java.time.temporal.TemporalAdjusters.*
import static org.strangeforest.tcb.dataload.LoaderUtil.*

loadRankings(new SqlPool())

static loadRankings(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
		def atpRankingsLoader = new ATPTourRankingsLoader(sql)
		def rankingService = new RankingsService(new NamedParameterJdbcTemplate(SqlPool.dataSource()))
		def lastDate = rankingService.getCurrentRankingDate(RankType.RANK)
		def currentDate = LocalDate.now().with(previousOrSame(MONDAY))
		for (def date = lastDate.with(next(MONDAY)); date <= currentDate; date = date.with(next(MONDAY))) {
			def formattedDate = date.format(ofPattern("yyyy-MM-dd"))
			println "Loading rankings for $formattedDate"
			def playerCount = 500
			retry(4, 0L, { th -> th instanceof SQLException }, { retry ->
				atpRankingsLoader.load(formattedDate, playerCount - 100 * retry)
			})
		}
	}
}