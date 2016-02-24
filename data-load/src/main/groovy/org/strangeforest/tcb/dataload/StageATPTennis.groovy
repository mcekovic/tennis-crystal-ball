package org.strangeforest.tcb.dataload

def sqlPool = new SqlPool()

def loader = new ATPTennisLoader()
loader.loadPlayers(new StagingPlayerLoader(sqlPool))
loader.loadRankings(new StagingRankingLoader(sqlPool))
loader.loadMatches(new StagingMatchLoader(sqlPool))
