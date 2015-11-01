package org.strangeforest.tcb.dataload

def sqls = SqlPool.create()

def loader = new ATPTennisLoader()
loader.loadPlayers(new StagingPlayerLoader(sqls))
loader.loadRankings(new StagingRankingLoader(sqls))
loader.loadMatches(new StagingMatchLoader(sqls))
