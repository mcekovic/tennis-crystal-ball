package org.strangeforest.tcb.dataload

import com.google.common.base.*

println 'Loading Tennis Data'
def stopwatch = Stopwatch.createStarted()

def sqlPool = new SqlPool()
def loader = new ATPTennisLoader()

loader.loadPlayers(new PlayerLoader(sqlPool))
sqlPool.withSql { sql -> loader.loadAdditionalPlayerData(sql) }

loader.loadRankings(new RankingLoader(sqlPool))
sqlPool.withSql { sql -> loader.loadAdditionalRankingData(sql) }
sqlPool.withSql { sql -> LoadAdHocRankings.loadRankings(sql) }

loader.loadMatches(new MatchLoader(sqlPool))
sqlPool.withSql { sql -> loader.loadAdditionalTournamentData(sql) }
LoadAdHocTournaments.loadTournaments(sqlPool)

sqlPool.withSql { sql -> loader.vacuum(sql) }

sqlPool.withSql { sql -> loader.correctData(sql) }
new EloRatings(sqlPool).compute(true)

sqlPool.withSql { sql -> loader.vacuum(sql) }

sqlPool.withSql { sql -> loader.refreshMaterializedViews(sql) }

sqlPool.withSql { sql -> loader.vacuum(sql) }

new RecordsLoader().loadRecords()

sqlPool.withSql { sql -> loader.vacuum(sql) }

println "Tennis Data loaded in $stopwatch"