package org.strangeforest.tcb.dataload

import javax.script.*

class TennisAbstractTournamentFetcher {

	static fetchTournament(int season, String urlId, def extId, String level = null) {
		def url = tournamentUrl(season, urlId)
		println "Fetching tournament URL '$url'"
		def manager = new ScriptEngineManager()
		def engine = manager.getEngineByName("JavaScript")
		def data = new URL(url).getText()
		engine.eval(data)

		def id = season + '-' + extId
		def name = engine.get('tname')
		def date = engine.get('tdate')
		def tLevel = level ? level : engine.get('tlev')
		def surface = engine.get('tsurf')
		def drawSize = engine.get('tsize')
		def matches = engine.get('matchmx')
		def records = []
		matches.each { m ->
			Map match = m.value
			String round = match['1']
			if (round == 'QF' || !round.startsWith('Q')) {
				def match_num = matchNum match['0']
				records << [
					'tourney_id': id,
					'match_num': match_num,
					'tourney_name': name,
					'tourney_date': date,
					'tourney_level': tLevel,
					'surface': surface,
					'draw_size': drawSize,
					'round': round,
					'score': match['2'],
					'best_of': match['3'],

					'winner_name': match['5'],
					'winner_rank': match['6'],
					'winner_seed': match['7'],
					'winner_entry': match['8'],
					'winner_hand': match['9'],
					'winner_age': null,
					'winner_ioc': match['11'],

					'loser_name': match['12'],
					'loser_rank': match['13'],
					'loser_seed': match['14'],
					'loser_entry': match['15'],
					'loser_hand': match['16'],
					'loser_age': null,
					'loser_ioc': match['18'],

					'minutes': match['19'],

					'w_ace': match['20'],
					'w_df': match['21'],
					'w_svpt': match['22'],
					'w_1stIn': match['23'],
					'w_1stWon': match['24'],
					'w_2ndWon': match['25'],
					'w_SvGms': match['26'],
					'w_bpSaved': match['27'],
					'w_bpFaced': match['28'],

					'l_ace': match['29'],
					'l_df': match['30'],
					'l_svpt': match['31'],
					'l_1stIn': match['32'],
					'l_1stWon': match['33'],
					'l_2ndWon': match['34'],
					'l_SvGms': match['35'],
					'l_bpSaved': match['36'],
					'l_bpFaced': match['37']
				]
			}
		}
		records
	}

	static tournamentUrl(int season, String urlId) {
		"http://www.minorleaguesplits.com/tennisabstract/cgi-bin/jstourneys/${season}${urlId}.js"
	}

	static matchNum(String match_id) {
		String.valueOf(Integer.parseInt(match_id) % 1000)
	}
}
