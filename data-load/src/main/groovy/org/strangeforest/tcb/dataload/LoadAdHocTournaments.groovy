package org.strangeforest.tcb.dataload

import static org.strangeforest.tcb.dataload.TournamentFetcher.*

def sqlPool = SqlPool.create()

def matchLoader = new MatchLoader(sqlPool)
matchLoader.load(fetchTournament(2016, 'Quito', 7161))
matchLoader.load(fetchTournament(2016, 'Montpellier', 375))
matchLoader.load(fetchTournament(2016, 'Sofia', 364))
matchLoader.load(fetchTournament(2016, 'Buenos_Aires', 506))
matchLoader.load(fetchTournament(2016, 'Rotterdam', 407))
matchLoader.load(fetchTournament(2016, 'Memphis', 402))
