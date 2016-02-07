package org.strangeforest.tcb.dataload

def sqlPool = SqlPool.create()

def loader = new ATPTennisLoader()
loader.loadPlayers(new PlayerLoader(sqlPool))
loader.loadRankings(new RankingLoader(sqlPool))
loader.loadMatches(new MatchLoader(sqlPool))

def sql = sqlPool.removeFirst()
//loader.loadAdditionalData(new AdditionalPlayerDataLoader(sql), 'player', 'classpath:/player-data.xml')
//loader.loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1990-tour-finals.xml')
//loader.loadAdditionalData(new XMLMatchLoader(sql), 'match', 'classpath:/tournaments/1981-monte-carlo.xml')

loader.refreshComputedData(sql)
