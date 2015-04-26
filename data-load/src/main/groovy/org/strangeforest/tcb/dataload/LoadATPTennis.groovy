package org.strangeforest.tcb.dataload

import groovy.sql.*

BASE_DIR = 'M:\\tennis-data\\tennis_atp\\'

db = [url:'jdbc:postgresql://localhost:5432/postgres', user:'tcb', password:'tcb', driver:'org.postgresql.Driver']
sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
sql.connection.autoCommit = false

println 'Loading ATP players'
new ATPPlayersLoader(sql).mergeFile(BASE_DIR + 'atp_players.csv')

println '\nLoading ATP rankings'
load {
	def loader = new ATPRankingsLoader(sql)
	def rows = 0
	for (decade in ['70s', '80s', '90s', '00s', '10s'])
		rows += loader.mergeFile(BASE_DIR + "atp_rankings_${decade}.csv")
	rows += loader.mergeFile(BASE_DIR + "atp_rankings_current.csv")
}

println '\nLoading ATP matches'
load {
	def loader = new ATPMatchesLoader(sql)
	def rows = 0
	for (year in 1968..2014)
		rows += loader.loadFile(BASE_DIR + "atp_matches_${year}.csv")
	def year = 2015
	rows += loader.mergeFile(BASE_DIR + "atp_matches_${year}.csv")
}

def load(loader) {
	def t0 = System.currentTimeMillis()

	def rows = loader()

	def seconds = (System.currentTimeMillis() - t0) / 1000.0
	int rowsPerSecond = rows / seconds
	println "Total rows: $rows in $seconds s ($rowsPerSecond row/s)"
}

