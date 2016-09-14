package org.strangeforest.tcb.dataload

def sqlPool = new SqlPool()

def loader = new ATPTennisLoader()

sqlPool.withSql { sql ->
	LoadAdHocRankings.loadRankings(sql)
}

LoadAdHocTournaments.loadTournaments(sqlPool)

sqlPool.withSql { sql ->
	loader.correctData(sql)
}

new EloRatings(sqlPool).compute(save = true, fullSave = false)

sqlPool.withSql { sql ->
	loader.refreshMaterializedViews(sql)
	loader.vacuum(sql)
}
