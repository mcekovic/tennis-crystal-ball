package org.strangeforest.tcb.dataload

import groovy.sql.*

def db = [url:'jdbc:postgresql://localhost:5432/postgres', user:'tcb', password:'tcb', driver:'org.postgresql.Driver']
def sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
sql.connection.autoCommit = false

def loader = new ATPTennisLoader()
loader.loadATPPlayers(new ATPPlayersLoader(sql))
loader.loadATPRankings(new ATPRankingsLoader(sql))
loader.loadATPMatches(new ATPMatchesLoader(sql))
