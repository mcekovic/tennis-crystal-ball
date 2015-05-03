package org.strangeforest.tcb.dataload

class ATPTennisLoader {

	private final String baseDir
	private final boolean full

	ATPTennisLoader() {
		baseDir = getBaseDir('tcb.data.base-dir')
		full = System.getProperty('tcb.data.full-load', 'true').toBoolean()
	}

	def loadPlayers(loader) {
		println 'Loading ATP players'
		loader.loadFile(baseDir + 'atp_players.csv')
		println()
	}

	def loadRankings(loader) {
		println 'Loading ATP rankings'
		load {
			def rows = 0
			if (full) {
				for (decade in ['70s', '80s', '90s', '00s-mc', '10s'])
					rows += loader.loadFile(baseDir + "atp_rankings_${decade}.csv")
			}
			rows += loader.loadFile(baseDir + "atp_rankings_current.csv")
		}
		println()
	}

	def loadMatches(loader) {
		println 'Loading ATP matches'
		load {
			def rows = 0
			if (full) {
				for (year in 1968..2014)
					rows += loader.loadFile(baseDir + "atp_matches_${year}.csv")
			}
			def year = 2015
			rows += loader.loadFile(baseDir + "atp_matches_${year}.csv")
		}
		println()
	}

	private static def load(loader) {
		def t0 = System.currentTimeMillis()

		def rows = loader()

		def seconds = (System.currentTimeMillis() - t0) / 1000.0
		int rowsPerSecond = rows / seconds
		println "Total rows: $rows in $seconds s ($rowsPerSecond row/s)"
	}

	private static def getBaseDir(property) {
		def baseDir = System.properties[property]
		if (!baseDir)
			throw new IllegalArgumentException('No ATP Tennis data base directory is set, please specify it in tcb.data.base-dir system property.')
		if (!baseDir.endsWith(File.separator))
			baseDir = baseDir + File.separator
		return baseDir
	}
}
