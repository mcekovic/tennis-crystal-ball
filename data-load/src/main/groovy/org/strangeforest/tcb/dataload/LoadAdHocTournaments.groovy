package org.strangeforest.tcb.dataload

import static org.strangeforest.tcb.dataload.TennisAbstractTournamentFetcher.*

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
	def matchLoader = new MatchLoader(sqlPool)
//	matchLoader.load(fetchTournament(2016, 'Quito', 7161))
	sqlPool.withSql {sql ->
		def atpWorldTourMatchLoader = new ATPWorldTourTournamentLoader(sql)
		atpWorldTourMatchLoader.loadTournament(2007, 'vina-del-mar', 505)
		atpWorldTourMatchLoader.loadTournament(2017, 'montpellier', 375)
		atpWorldTourMatchLoader.loadTournament(2017, 'quito', 7161)
		atpWorldTourMatchLoader.loadTournament(2017, 'sofia', 7434)
	}
}