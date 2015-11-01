package org.strangeforest.tcb.dataload

def sqlPool = SqlPool.create()

def loader = new ATPTennisLoader()
loader.loadPlayers(new PlayerLoader(sqlPool))
loader.loadRankings(new RankingLoader(sqlPool))
loader.loadMatches(new MatchLoader(sqlPool))

def sql = sqlPool.removeFirst()
loader.loadAdditionalPlayerData(new AdditionalPlayerDataLoader(sql))

loader.refreshComputedData(sql)
