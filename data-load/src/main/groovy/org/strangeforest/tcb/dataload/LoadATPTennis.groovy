package org.strangeforest.tcb.dataload

import groovy.sql.*

def baseDir = getBaseDir('tcb.data.base-dir')
def full = System.getProperty('tcb.data.full-load', 'true').toBoolean()

def db = [url:'jdbc:postgresql://localhost:5432/postgres', user:'tcb', password:'tcb', driver:'org.postgresql.Driver']
def sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
sql.connection.autoCommit = false

loadATPPlayers(baseDir, sql)
loadATPRankings(baseDir, sql, full)
loadATPMatches(baseDir, sql, full)

def loadATPPlayers(baseDir, sql) {
	println 'Loading ATP players'
	new ATPPlayersLoader(sql).mergeFile(baseDir + 'atp_players.csv')
	println()
}

def loadATPRankings(baseDir, sql, full) {
	println 'Loading ATP rankings'
	load {
		def loader = new ATPRankingsLoader(sql)
		def rows = 0
		if (full) {
			for (decade in ['70s', '80s', '90s', '00s', '10s'])
				rows += loader.mergeFile(baseDir + "atp_rankings_${decade}.csv")
		}
		rows += loader.mergeFile(baseDir + "atp_rankings_current.csv")
	}
	println()
}

def loadATPMatches(baseDir, sql, full) {
	println 'Loading ATP matches'
	load {
		def loader = new ATPMatchesLoader(sql)
		def rows = 0
		if (full) {
			for (year in 1968..2014)
				rows += loader.loadFile(baseDir + "atp_matches_${year}.csv")
		}
		def year = 2015
		rows += loader.mergeFile(baseDir + "atp_matches_${year}.csv")
	}
	println()
}

def load(loader) {
	def t0 = System.currentTimeMillis()

	def rows = loader()

	def seconds = (System.currentTimeMillis() - t0) / 1000.0
	int rowsPerSecond = rows / seconds
	println "Total rows: $rows in $seconds s ($rowsPerSecond row/s)"
}

def getBaseDir(property) {
	def baseDir = System.properties[property]
	if (!baseDir)
		throw new IllegalArgumentException('No ATP Tennis data base directory is set, please specify it in tcb.data.base-dir system property.')
	if (!baseDir.endsWith(File.separator))
		baseDir = baseDir + File.separator
	return baseDir
}
