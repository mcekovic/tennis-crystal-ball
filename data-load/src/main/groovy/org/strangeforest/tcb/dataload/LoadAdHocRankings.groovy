package org.strangeforest.tcb.dataload

import groovy.sql.*

def sqlPool = new SqlPool()
sqlPool.withSql { Sql sql ->
	loadRankings(sql)
}

static loadRankings(Sql sql) {
	def rankingsLoader = new ATPTourRankingsLoader(sql)
//	rankingsLoader.rankDates().collect {
//		if (it.startsWith('1984'))
//			println("$it: ${rankingsLoader.playerCount(it)}")
//	}
//	rankingsLoader.load('1984-06-18', 500)
}
