package org.strangeforest.tcb.dataload

import java.time.*

import org.strangeforest.tcb.stats.model.elo.*

import groovy.sql.*

import static org.strangeforest.tcb.dataload.LoadParams.*

def sqlPool = new SqlPool()

def eloRatings = new EloRatingsManager(SqlPool.connectionPoolDataSource())
eloRatings.compute(true, getBooleanProperty(FULL_LOAD_PROPERTY, FULL_LOAD_DEFAULT), null, SqlPool.connections)

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
