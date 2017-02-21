package org.strangeforest.tcb.dataload

import static org.strangeforest.tcb.dataload.TennisAbstractTournamentFetcher.*

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
//	def matchLoader = new MatchLoader(sqlPool)
//	matchLoader.load(fetchTournament(2016, 'Quito', 7161))
	sqlPool.withSql {sql ->
		def atpWorldTourMatchLoader = new ATPWorldTourTournamentLoader(sql)
		atpWorldTourMatchLoader.loadTournament(1968, 'manchester', 7310, null, false, ['R16', 'R32', 'R64'])
		atpWorldTourMatchLoader.loadTournament(1968, 'london', 311)
		atpWorldTourMatchLoader.loadTournament(1968, 'dublin', 2029, null, false, ['R16', 'R32'])
		atpWorldTourMatchLoader.loadTournament(1969, 'perth', 243, null, false, ['QF', 'R16', 'R32'])
		atpWorldTourMatchLoader.loadTournament(1969, 'hobart', 713)
		atpWorldTourMatchLoader.loadTournament(1969, 'new-york', 2058)
		atpWorldTourMatchLoader.loadTournament(1969, 'brussels', 406)
		atpWorldTourMatchLoader.loadTournament(1969, 'dublin', 2029, null, false, ['R32'])
		atpWorldTourMatchLoader.loadTournament(1969, 'las-vegas', 413)
		atpWorldTourMatchLoader.loadTournament(1970, 'north-miami-beach', 681)
		atpWorldTourMatchLoader.loadTournament(1970, 'richmond', 802)
		atpWorldTourMatchLoader.loadTournament(1970, 'chorpus-christi', 6816)
		atpWorldTourMatchLoader.loadTournament(1970, 'los-angeles', 6811)
		atpWorldTourMatchLoader.loadTournament(1970, 'macon', 2066)
		atpWorldTourMatchLoader.loadTournament(1970, 'szczecin', 657)
		atpWorldTourMatchLoader.loadTournament(1970, 'wembley', 6812)
		atpWorldTourMatchLoader.loadTournament(1970, 'san-juan', 6817, null, false, ['R64'])
		atpWorldTourMatchLoader.loadTournament(1970, 'durban', 260, null, false, ['R32', 'R64'])
		atpWorldTourMatchLoader.loadTournament(1970, 'st-petersburg', 6818, null, false, ['R32', 'R64'])
		atpWorldTourMatchLoader.loadTournament(1970, 'bermuda', 436, null, false, ['R32'])
		atpWorldTourMatchLoader.loadTournament(1970, 'dallas', 388)
		atpWorldTourMatchLoader.loadTournament(1970, 'houston', 405, null, false, ['R32'])
		atpWorldTourMatchLoader.loadTournament(1970, 'bournemouth', 347, null, false, ['R32'])
		atpWorldTourMatchLoader.loadTournament(1970, 'atlanta', 412)
		atpWorldTourMatchLoader.loadTournament(1970, 'las-vegas', 413)
		atpWorldTourMatchLoader.loadTournament(1970, 'brussels', 406)
		atpWorldTourMatchLoader.loadTournament(1970, 'manchester', 7310, null, false, ['QF', 'R16', 'R32', 'R64'])
		atpWorldTourMatchLoader.loadTournament(1970, 'bristol', 313)
		atpWorldTourMatchLoader.loadTournament(1970, 'eastbourne', 2049, null, false, ['R32'])
		atpWorldTourMatchLoader.loadTournament(2007, 'vina-del-mar', 505)

		atpWorldTourMatchLoader.loadTournament(2017, 'montpellier', 375)
		atpWorldTourMatchLoader.loadTournament(2017, 'quito', 7161)
		atpWorldTourMatchLoader.loadTournament(2017, 'sofia', 7434)
		atpWorldTourMatchLoader.loadTournament(2017, 'buenos-aires', 506)
		atpWorldTourMatchLoader.loadTournament(2017, 'memphis', 402)
		atpWorldTourMatchLoader.loadTournament(2017, 'rotterdam', 407)
	}
}