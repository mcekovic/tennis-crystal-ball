package org.strangeforest.tcb.dataload

import groovy.sql.*

def sqlPool = new SqlPool()
sqlPool.withSql { Sql sql ->
	loadRankings(sql)
}

static loadRankings(Sql sql) {
	def rankingsLoader = new ATPWorldTourRankingsLoader(sql)
	rankingsLoader.load('2017-02-13', 200)
	rankingsLoader.load('2017-02-20', 200)
}
