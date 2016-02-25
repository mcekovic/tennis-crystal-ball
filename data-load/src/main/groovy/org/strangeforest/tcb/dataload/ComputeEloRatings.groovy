package org.strangeforest.tcb.dataload

import groovy.sql.*

def sqlPool = new SqlPool()

def eloRatings = new EloRatings(sqlPool)
eloRatings.compute(true)


sqlPool.withSql { sql ->
	showCurrent(eloRatings, sql)
	showAllTime(eloRatings, sql)
}



def showCurrent(EloRatings eloRatings, Sql sql) {
	printf '%n%1$4s %2$-30s %3$4s%n', 'Rank', 'Player', 'Elo'
	def i = 0
	eloRatings.current(100).each {
		printf '%1$4s %2$-30s %3$4s%n', ++i, getPlayerName(sql, it.key), it.value
	}
}

def showAllTime(EloRatings eloRatings, Sql sql) {
	printf '%n%1$4s %2$-30s %3$4s %4$10s %5$4s/%6$4s%n', 'Rank', 'Player', 'Elo', 'Date', 'PkMt', 'AlMt'
	def i = 0
	eloRatings.allTime(100).each {
		def rating = it.value
		def bestRating = rating.bestRating
		printf '%1$4s %2$-30s %3$4s %4$10s %5$4s/%6$4s%n', ++i, getPlayerName(sql, it.key), bestRating, bestRating.date.format('dd-MM-yyyy'), bestRating.matches, rating.matches
	}
}

def getPlayerName(Sql sql, playerId) {
	sql.firstRow('SELECT name FROM player_v WHERE player_id = ?', [playerId]).name
}
