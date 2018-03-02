package org.strangeforest.tcb.dataload

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
//	def loader = new ATPTennisLoader()
//	loader.loadAdditionalTournament(sqlPool, 'classpath:/tournaments/2017-davis-cup.xml')
//	loader.loadAdditionalTournament(sqlPool, 'classpath:/tournaments/2017-wimbledon.xml')
	sqlPool.withSql {sql ->
		def atpWorldTourMatchLoader = new ATPWorldTourTournamentLoader(sql)
		atpWorldTourMatchLoader.loadTournament(2017, 'dubai', 495)
//		def atpInProgressTournamentLoader = new ATPWorldTourInProgressTournamentLoader(sql)
//		atpInProgressTournamentLoader.loadAndForecastTournament('indian-wells', 404, 2017, null, null, true)
	}
}