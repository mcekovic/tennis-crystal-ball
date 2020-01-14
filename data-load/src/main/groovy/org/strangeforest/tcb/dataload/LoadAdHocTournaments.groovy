package org.strangeforest.tcb.dataload

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
//		def xmlMatchLoader = new XMLMatchLoader(sql)
//		xmlMatchLoader.withLoadStats().loadFile('classpath:/tournaments/2020-atp-cup.xml')
//		def atpTourMatchLoader = new ATPTourTournamentLoader(sql)
//		atpTourMatchLoader.loadTournament(2019, 'dubai', 495)
//		def atpInProgressTournamentLoader = new ATPTourInProgressTournamentLoader(sql)
//		atpInProgressTournamentLoader.loadAndForecastTournament('indian-wells', 404, 2017, null, null, true)
	}
}