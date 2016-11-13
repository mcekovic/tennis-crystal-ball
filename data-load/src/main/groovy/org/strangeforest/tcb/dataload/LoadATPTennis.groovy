package org.strangeforest.tcb.dataload

def sqlPool = new SqlPool()

def loader = new ATPTennisLoader()
loader.loadPlayers(new PlayerLoader(sqlPool))
loader.loadRankings(new RankingLoader(sqlPool))
loader.loadMatches(new MatchLoader(sqlPool))

sqlPool.withSql { sql ->
	loader.loadAdditionalPlayerData(sql)
	loader.loadAdditionalRankingData(sql)
	LoadAdHocRankings.loadRankings(sql)
	loader.loadAdditionalTournamentData(sql)
}

LoadAdHocTournaments.loadTournaments(sqlPool)

sqlPool.withSql { sql -> loader.correctData(sql) }

new EloRatings(sqlPool).compute(true)

sqlPool.withSql { sql -> loader.refreshMaterializedViews(sql) }

new RecordsLoader().loadRecords()

sqlPool.withSql { sql -> loader.vacuum(sql) }