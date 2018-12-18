package org.strangeforest.tcb.dataload

import java.time.*

import org.strangeforest.tcb.stats.model.elo.*

import groovy.sql.*

import static org.strangeforest.tcb.dataload.LoadParams.*

def eloRatings = EloRatingsRunner.computeEloRatings()

def sqlPool = new SqlPool()

if (getBooleanProperty(VERBOSE_PROPERTY, VERBOSE_DEFAULT)) {
	sqlPool.withSql { sql ->
		showCurrent(eloRatings, sql)
	}
}

def showCurrent(EloRatingsManager eloRatings, Sql sql) {
	printf '%n%1$4s %2$-30s %3$4s%n', 'Rank', 'Player', 'Elo'
	def i = 0
	eloRatings.getRatings('E', 100, LocalDate.now()).each {
		printf '%1$4s %2$-30s %3$4s%n', ++i, getPlayerName(sql, it.playerId), it
	}
}

def getPlayerName(Sql sql, int playerId) {
	sql.firstRow('SELECT name FROM player_v WHERE player_id = ?', [playerId]).name
}
