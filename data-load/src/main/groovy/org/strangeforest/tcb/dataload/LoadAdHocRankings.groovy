package org.strangeforest.tcb.dataload

import groovy.sql.*

def sqlPool = new SqlPool()
sqlPool.withSql { Sql sql ->
	loadRankings(sql)
}

static loadRankings(Sql sql) {
	def rankingsLoader = new ATPTourRankingsLoader(sql)
	rankingsLoader.load('1994-12-26', 500)
}
