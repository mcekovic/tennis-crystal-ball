package org.strangeforest.tcb.dataload

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
//		def xmlMatchLoader = new XMLMatchLoader(sql)
//		xmlMatchLoader.loadFile('classpath:/tournaments/2020-roland-garros.xml')
		def atpTourMatchLoader = new ATPTourTournamentLoader(sql)
		atpTourMatchLoader.loadTournament(2020, 'kitzbuhel', 319)
//		def atpInProgressTournamentLoader = new ATPTourInProgressTournamentLoader(sql)
//		atpInProgressTournamentLoader.loadAndForecastTournament('indian-wells', 404, 2017, null, null, true)
	}
}