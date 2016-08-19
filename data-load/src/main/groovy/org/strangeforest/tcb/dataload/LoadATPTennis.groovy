package org.strangeforest.tcb.dataload

def sqlPool = new SqlPool()

def loader = new ATPTennisLoader()
loader.loadPlayers(new PlayerLoader(sqlPool))
loader.loadRankings(new RankingLoader(sqlPool))
loader.loadMatches(new MatchLoader(sqlPool))

sqlPool.withSql { sql ->
	loader.loadAdditionalPlayerData(sql)

	loader.loadAdditionalRankingData(sql)
	new LoadAdHocRankings().run()

	loader.loadAdditionalTournamentData(sql)
	new LoadAdHocTournaments().run()

	loader.correctData(sql)
	new ComputeEloRatings().run()

	loader.refreshMaterializedViews(sql)
	loader.vacuum()
}
