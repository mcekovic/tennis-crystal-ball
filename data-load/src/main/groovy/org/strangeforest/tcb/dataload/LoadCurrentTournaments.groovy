package org.strangeforest.tcb.dataload

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
		def atpCurrentTournamentLoader = new ATPWorldTourCurrentTournamentLoader(sql)
		atpCurrentTournamentLoader.loadTournament('miami', 403)
	}
}