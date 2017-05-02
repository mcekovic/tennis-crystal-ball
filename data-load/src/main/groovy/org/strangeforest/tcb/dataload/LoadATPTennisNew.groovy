package org.strangeforest.tcb.dataload

import com.google.common.base.*

println 'Loading New Tennis Data'
def stopwatch = Stopwatch.createStarted()

def sqlPool = new SqlPool()
def loader = new ATPTennisLoader()

LoadNewRankings.loadRankings(sqlPool)
LoadNewTournaments.loadTournaments(sqlPool)

sqlPool.withSql { sql -> loader.correctData(sql) }
new EloRatings(sqlPool).compute(save = true, fullSave = false)
sqlPool.withSql { sql -> loader.refreshMaterializedViews(sql) }

sqlPool.withSql { sql -> loader.vacuum(sql) }

new RecordsLoader().loadRecords()

sqlPool.withSql { sql -> loader.vacuum(sql) }

println "New Tennis Data loaded in $stopwatch"