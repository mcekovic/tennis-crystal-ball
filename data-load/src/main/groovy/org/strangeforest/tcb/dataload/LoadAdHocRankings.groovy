package org.strangeforest.tcb.dataload

import groovy.sql.*

def sqlPool = new SqlPool()
sqlPool.withSql { Sql sql ->
	loadRankings(sql)
}

static loadRankings(Sql sql) {
	def rankingsLoader = new ATPWorldTourRankingsLoader(sql)
	rankingsLoader.load('2017-02-13', 500)
	rankingsLoader.load('2017-02-20', 500)
	rankingsLoader.load('2017-02-27', 500)
	rankingsLoader.load('2017-03-06', 500)
	rankingsLoader.load('2017-03-20', 500)
	rankingsLoader.load('2017-04-03', 500)
	rankingsLoader.load('2017-04-10', 500)
	rankingsLoader.load('2017-04-17', 500)
}
