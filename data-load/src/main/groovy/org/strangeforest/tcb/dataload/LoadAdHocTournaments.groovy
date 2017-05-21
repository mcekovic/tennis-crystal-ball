package org.strangeforest.tcb.dataload

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
//	def loader = new ATPTennisLoader()
//	loader.loadAdditionalTournament(sqlPool, 'classpath:/tournaments/2017-davis-cup.xml')
	sqlPool.withSql {sql ->
		def atpWorldTourMatchLoader = new ATPWorldTourTournamentLoader(sql)
		atpWorldTourMatchLoader.loadTournament(2017, 'montpellier', 375)
		atpWorldTourMatchLoader.loadTournament(2017, 'quito', 7161)
		atpWorldTourMatchLoader.loadTournament(2017, 'sofia', 7434)
		atpWorldTourMatchLoader.loadTournament(2017, 'buenos-aires', 506)
		atpWorldTourMatchLoader.loadTournament(2017, 'memphis', 402)
		atpWorldTourMatchLoader.loadTournament(2017, 'rotterdam', 407)
		atpWorldTourMatchLoader.loadTournament(2017, 'delray-beach', 499)
		atpWorldTourMatchLoader.loadTournament(2017, 'marseille', 496)
		atpWorldTourMatchLoader.loadTournament(2017, 'rio-de-janeiro', 6932)
		atpWorldTourMatchLoader.loadTournament(2017, 'acapulco', 807)
		atpWorldTourMatchLoader.loadTournament(2017, 'dubai', 495)
		atpWorldTourMatchLoader.loadTournament(2017, 'sao-paulo', 533)
		atpWorldTourMatchLoader.loadTournament(2017, 'indian-wells', 404)
		atpWorldTourMatchLoader.loadTournament(2017, 'miami', 403)
		atpWorldTourMatchLoader.loadTournament(2017, 'marrakech', 360)
		atpWorldTourMatchLoader.loadTournament(2017, 'houston', 717)

//		def atpInProgressTournamentLoader = new ATPWorldTourInProgressTournamentLoader(sql)
//		atpInProgressTournamentLoader.loadAndSimulateTournament('indian-wells', 404, 2017, null, null, true)
//		atpInProgressTournamentLoader.loadAndSimulateTournament('miami', 403, 2017, null, null, true)
	}
}