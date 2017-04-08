package org.strangeforest.tcb.dataload

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
		def atpInProgressTournamentLoader = new ATPWorldTourInProgressTournamentLoader(sql)
//		atpInProgressTournamentLoader.loadAndSimulateTournament('indian-wells', 404, 2017)
//		atpInProgressTournamentLoader.loadAndSimulateTournament('miami', 403, 2017)
		atpInProgressTournamentLoader.loadAndSimulateTournament('houston', 717)
		atpInProgressTournamentLoader.loadAndSimulateTournament('marrakech', 360)
	}
}