package org.strangeforest.tcb.dataload

import groovy.sql.Sql

def sqlPool = new SqlPool()

sqlPool.withSql { Sql sql ->
	sql.rows('SELECT match_id, score FROM match WHERE match_id = 160432 ORDER BY date, match_num').each { match ->
		def scoreStr = match.score
		def score = MatchScoreParser.parse(scoreStr)
		def scoreStr2 = score?.toString()
		if ((scoreStr || scoreStr2) && scoreStr2 != scoreStr)
			println "$scoreStr <> $scoreStr2 [${match.match_id}]"
	}
}
