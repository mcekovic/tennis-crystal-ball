package org.strangeforest.tcb.dataload

import groovy.sql.*

def sqlPool = new SqlPool()
sqlPool.withSql { Sql sql ->
	loadRankings(sql)
}

static loadRankings(Sql sql) {
	def rankingsLoader = new ATPTourRankingsLoader(sql)
	println 'Fixing invalid rankings...'
	rankingsLoader.load('1994-12-26', 500)
	rankingsLoader.load('1996-09-02', 500)
	rankingsLoader.load('1996-12-23', 500)
	rankingsLoader.load('1996-12-30', 500)
	rankingsLoader.load('1997-01-20', 500)
	rankingsLoader.load('1997-03-24', 500)
	rankingsLoader.load('1997-06-02', 500)
	rankingsLoader.load('1997-06-30', 500)
	rankingsLoader.load('1997-09-01', 500)
	rankingsLoader.load('1997-12-22', 500)
	rankingsLoader.load('1997-12-29', 500)
	rankingsLoader.load('1998-01-05', 500)
	rankingsLoader.load('1998-01-26', 500)
	rankingsLoader.load('1998-03-23', 500)
	rankingsLoader.load('1998-06-01', 500)
	rankingsLoader.load('1998-06-29', 500)
	rankingsLoader.load('1998-09-07', 500)
	rankingsLoader.load('1998-12-21', 500)
	rankingsLoader.load('1998-12-28', 500)
	rankingsLoader.load('1999-01-25', 500)
	rankingsLoader.load('1999-03-22', 500)
	rankingsLoader.load('1999-05-31', 500)
	rankingsLoader.load('1999-06-28', 500)
	rankingsLoader.load('1999-09-06', 500)
	rankingsLoader.load('1999-12-20', 500)
	rankingsLoader.load('1999-12-27', 500)
	rankingsLoader.load('2000-01-10', 500)
}
