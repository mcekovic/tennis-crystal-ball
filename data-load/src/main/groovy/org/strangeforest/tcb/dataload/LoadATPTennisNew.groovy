package org.strangeforest.tcb.dataload

import com.google.common.base.*

println 'Loading New Tennis Data'
def stopwatch = Stopwatch.createStarted()

def sqlPool = new SqlPool()
def loader = new ATPTennisLoader()

LoadNewRankings.loadRankings(sqlPool)
LoadNewTournaments.loadTournaments(sqlPool)

EloRatingsRunner.computeEloRatings(false)

sqlPool.withSql { sql -> loader.correctData(sql) }
sqlPool.withSql { sql -> loader.refreshMaterializedViews(sql) }

sqlPool.withSql { sql -> loader.vacuum(sql) }

sqlPool.withSql { sql -> new RecordsLoader().loadRecords(loader, sql) }

LoadInProgressTournaments.loadTournaments(sqlPool)

sqlPool.withSql { sql -> loader.vacuum(sql) }

println "New Tennis Data loaded in $stopwatch"