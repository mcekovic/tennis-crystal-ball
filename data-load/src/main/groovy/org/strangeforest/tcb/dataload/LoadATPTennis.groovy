package org.strangeforest.tcb.dataload

import groovy.sql.*

def dbURL = System.getProperty('tcb.db.url', 'jdbc:postgresql://localhost:5432/postgres')
def username = System.getProperty('tcb.db.username', 'tcb')
def password = System.getProperty('tcb.db.password', 'tcb')

def db = [url: dbURL, user: username, password: password, driver: 'org.postgresql.Driver']
def sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
sql.connection.autoCommit = false

def loader = new ATPTennisLoader()
loader.loadPlayers(new PlayerLoader(sql))
loader.loadRankings(new RankingLoader(sql))
loader.loadMatches(new MatchLoader(sql))

loader.loadAdditionalPlayerData(new AdditionalPlayerDataLoader(sql))

loader.refreshComputedData(sql)
