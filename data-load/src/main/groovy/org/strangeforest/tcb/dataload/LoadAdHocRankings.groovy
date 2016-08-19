package org.strangeforest.tcb.dataload

import groovy.sql.*

def sqlPool = new SqlPool()
sqlPool.withSql { Sql sql ->
	def rankingsLoader = new ATPWorldTourRankingsLoader(sql)
	rankingsLoader.load('2016-02-08', 200)
	rankingsLoader.load('2016-02-15', 200)
	rankingsLoader.load('2016-02-22', 200)
	rankingsLoader.load('2016-02-29', 200)
	rankingsLoader.load('2016-03-07', 200)
	rankingsLoader.load('2016-03-21', 200)
	rankingsLoader.load('2016-04-04', 200)
	rankingsLoader.load('2016-04-11', 200)
	rankingsLoader.load('2016-04-18', 200)
	rankingsLoader.load('2016-04-25', 200)
	rankingsLoader.load('2016-05-02', 200)
	rankingsLoader.load('2016-05-09', 200)
	rankingsLoader.load('2016-05-16', 200)
	rankingsLoader.load('2016-05-23', 200)
	rankingsLoader.load('2016-06-06', 200)
	rankingsLoader.load('2016-06-13', 200)
	rankingsLoader.load('2016-06-20', 200)
	rankingsLoader.load('2016-06-27', 200)
	rankingsLoader.load('2016-07-11', 200)
	rankingsLoader.load('2016-07-18', 200)
	rankingsLoader.load('2016-07-25', 200)
	rankingsLoader.load('2016-08-01', 200)
	rankingsLoader.load('2016-08-08', 200)
	rankingsLoader.load('2016-08-15', 200)
}
