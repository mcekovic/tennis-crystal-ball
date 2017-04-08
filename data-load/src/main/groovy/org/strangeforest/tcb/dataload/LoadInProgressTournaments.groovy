package org.strangeforest.tcb.dataload

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
		def atpInProgressTournamentLoader = new ATPWorldTourInProgressTournamentLoader(sql)
//		atpInProgressTournamentLoader.loadAndSimulateTournament('indian-wells', 404)
//		atpInProgressTournamentLoader.loadAndSimulateTournament('miami', 403)
		atpInProgressTournamentLoader.loadAndSimulateTournament('houston', 717)
	}
}