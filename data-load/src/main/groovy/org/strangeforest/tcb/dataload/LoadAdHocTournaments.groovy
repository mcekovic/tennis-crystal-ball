package org.strangeforest.tcb.dataload

import static org.strangeforest.tcb.dataload.TournamentFetcher.*

def sqlPool = SqlPool.create()

def matchLoader = new MatchLoader(sqlPool)
matchLoader.load(fetchTournament(2015, 'Metz', 341))
matchLoader.load(fetchTournament(2015, 'St._Petersburg', 568))
matchLoader.load(fetchTournament(2015, 'Kuala_Lumpur', 6003))
matchLoader.load(fetchTournament(2015, 'Shenzhen', 6967))
matchLoader.load(fetchTournament(2015, 'Beijing', 747))
matchLoader.load(fetchTournament(2015, 'Tokyo', 329))
matchLoader.load(fetchTournament(2015, 'Shanghai', 5014, 'M'))
matchLoader.load(fetchTournament(2015, 'Moscow', 438))
matchLoader.load(fetchTournament(2015, 'Stockholm', 429))
matchLoader.load(fetchTournament(2015, 'Vienna', 337))
matchLoader.load(fetchTournament(2015, 'Basel', 328))
matchLoader.load(fetchTournament(2015, 'Valencia', 573))
matchLoader.load(fetchTournament(2015, 'Paris', 352, 'M'))