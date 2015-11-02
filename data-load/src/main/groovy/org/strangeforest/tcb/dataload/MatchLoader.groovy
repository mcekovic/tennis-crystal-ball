package org.strangeforest.tcb.dataload

import groovy.sql.*

import java.util.concurrent.*

class MatchLoader extends BaseCSVLoader {

	MatchLoader(BlockingDeque<Sql> sqlPool) {
		super(sqlPool)
	}

	int threadCount() { 1 }

	String loadSql() {
		'{call load_match(' +
			':ext_tournament_id, :season, :tournament_date, :tournament_name, :event_name, :tournament_level, :surface, :indoor, :draw_size, :rank_points, ' +
			':match_num, :round, :best_of, ' +
			':ext_winner_id, :winner_seed, :winner_entry, :winner_rank, :winner_rank_points, :winner_age, :winner_country_id, :winner_name, :winner_height, :winner_hand, ' +
			':ext_loser_id, :loser_seed, :loser_entry, :loser_rank, :loser_rank_points, :loser_age, :loser_country_id, :loser_name, :loser_height, :loser_hand, ' +
			':score, :w_sets, :l_sets, :outcome, :w_gems, :l_gems, :w_tb_pt, :l_tb_pt, :minutes, ' +
			':w_ace, :w_df, :w_sv_pt, :w_1st_in, :w_1st_won, :w_2nd_won, :w_sv_gms, :w_bp_sv, :w_bp_fc, ' +
			':l_ace, :l_df, :l_sv_pt, :l_1st_in, :l_1st_won, :l_2nd_won, :l_sv_gms, :l_bp_sv, :l_bp_fc' +
		')}'
	}

	int batchSize() { 100 }

	Map params(line, sql) {
		def params = [:]

		def tourneyId = string line.tourney_id
		if (tourneyId[4] != '-')
			throw new IllegalArgumentException("Invalid tourney_id: $tourneyId")
		def extTourneyId = string(tourneyId.substring(5))
		def level = string line.tourney_level
		def name = string line.tourney_name
		def dcInfo = level == 'D' ? extractDCTournamentInfo(name) : null;
		def season = smallint(tourneyId.substring(0, 4))
		params.season = season
		params.tournament_date = date line.tourney_date


		def eventName = level != 'D' ? name : dcInfo.name
		params.tournament_name = level != 'O' ? eventName : 'Olympics'
		params.event_name = eventName
		def drawSize = smallint line.draw_size
		def mappedLevel = mapLevel(level, drawSize, name, season, extTourneyId)
		params.ext_tournament_id = mapExtTournamentId(extTourneyId, mappedLevel, dcInfo)
		params.tournament_level = mappedLevel
		def surface = string line.surface
		params.surface = mapSurface surface
		params.indoor = mapIndoor surface
		params.draw_size = drawSize
		params.rank_points = mapRankPoints mappedLevel

		def matchNum = line.match_num
		params.match_num = level != 'D' ? smallint(matchNum) : smallint(dcMatchNum(extTourneyId, matchNum))
		def round = string line.round
		params.round = level != 'D' ? mapRound(round) : dcInfo.round
		params.best_of = smallint line.best_of

		params.ext_winner_id = integer line.winner_id
		params.winner_seed = smallint line.winner_seed
		params.winner_entry = mapEntry(string(line.winner_entry))
		params.winner_rank = integer line.winner_rank
		params.winner_rank_points = integer line.winner_rank_points
		params.winner_age = real line.winner_age
		params.winner_country_id = country line.winner_ioc
		params.winner_name = string line.winner_name
		params.winner_height = smallint line.winner_ht
		params.winner_hand = hand line.winner_hand
		
		params.ext_loser_id = integer line.loser_id
		params.loser_seed = smallint line.loser_seed
		params.loser_entry = mapEntry(string(line.loser_entry))
		params.loser_rank = integer line.loser_rank
		params.loser_rank_points = integer line.loser_rank_points
		params.loser_age = real line.loser_age
		params.loser_country_id = country line.loser_ioc
		params.loser_name = string line.loser_name
		params.loser_height = smallint line.loser_ht
		params.loser_hand = hand line.loser_hand

		def score = line.score.trim()
		def matchScore = MatchScore.parse(score)
		params.score = string score
		params.w_sets = matchScore?.w_sets
		params.l_sets = matchScore?.l_sets
		params.outcome = matchScore?.outcome
		params.w_gems = matchScore ? shortArray(sql, matchScore.w_gems) : null
		params.l_gems = matchScore ? shortArray(sql, matchScore.l_gems) : null
		params.w_tb_pt = matchScore ? shortArray(sql, matchScore.w_tb_pt) : null
		params.l_tb_pt = matchScore ? shortArray(sql, matchScore.l_tb_pt) : null

		params.minutes = smallint line.minutes

		params.w_ace = smallint line.w_ace
		params.w_df = smallint line.w_df
		params.w_sv_pt = smallint line.w_svpt
		params.w_1st_in = smallint line.w_1stIn
		params.w_1st_won = smallint line.w_1stWon
		params.w_2nd_won = smallint line.w_2ndWon
		params.w_sv_gms = smallint line.w_SvGms
		params.w_bp_sv = smallint line.w_bpSaved
		params.w_bp_fc = smallint line.w_bpFaced

		params.l_ace = smallint line.l_ace
		params.l_df = smallint line.l_df
		params.l_sv_pt = smallint line.l_svpt
		params.l_1st_in = smallint line.l_1stIn
		params.l_1st_won = smallint line.l_1stWon
		params.l_2nd_won = smallint line.l_2ndWon
		params.l_sv_gms = smallint line.l_SvGms
		params.l_bp_sv = smallint line.l_bpSaved
		params.l_bp_fc = smallint line.l_bpFaced
		return params
	}

	static def mapExtTournamentId(String extTourneyId, String level, DavisCupTournamentInfo dcInfo) {
		switch (level) {
			case 'D': return dcInfo.extId
			case 'O': return level
			default: return extTourneyId
		}
	}

	static def mapLevel(String level, short drawSize, String name, int season, String extTournamentId) {
		switch (level) {
			case 'G': return 'G'
			case 'F': return name.contains("WCT") ? 'A' : 'F'
			case 'M': return name.startsWith('Masters') && drawSize <= 16 ? 'F' : 'M'
			case 'A':
				if (name.contains('Olympics'))
					return 'O'
				else if (name.startsWith('Australian Open') && season == 1977)
					return 'G'
				else if (
					(name.startsWith('Boston') && (1970..1977).contains(season)) ||
					(name.startsWith('Buenos Aires') && (1970..1971).contains(season)) ||
					(name.startsWith('Forest Hills') && (1982..1985).contains(season)) ||
					(name.equals('Hamburg') && (1978..1989).contains(season)) ||
					(name.equals('Indianapolis') && (1969..1978).contains(season)) ||
					(name.equals('Johannesburg') && (1972..1975).contains(season)) ||
					(name.startsWith('Las Vegas') && (1976..1981).contains(season)) ||
					(name.startsWith('Monte Carlo') && (1968..1989).contains(season)) ||
					(name.startsWith('Monte-Carlo') && season == 2015) ||
					(name.startsWith('Paris') && season == 1989) ||
					(name.startsWith('Philadelphia') && (1968..1986).contains(season)) ||
					(name.equals('Stockholm') && ((1972..1980).contains(season) || (1984..1989).contains(season))) ||
					(name.equals('Tokyo Indoor') && (1978..1988).contains(season)) ||
					(name.startsWith('Washington') && extTournamentId.equals('418') && (1971..1978).contains(season)) ||
					(name.equals('Wembley') && (1976..1983).contains(season))
				)
					return 'M'
				else
					return 'A'
			case 'D': return 'D'
			case 'C': return 'C'
			default: throw new IllegalArgumentException("Unknown tournament level: $level")
		}
	}

	static def mapSurface(String surface) {
		switch (surface) {
			case 'Hard': return 'H'
			case 'Clay': return 'C'
			case 'Grass': return 'G'
			case 'Carpet': return 'P'
			default: return null
		}
	}

	static def mapIndoor(String surface) {
		switch (surface) {
			case 'Carpet': return true
			default: return false
		}
	}

	static def mapRound(String round) {
		switch (round) {
			case 'R63': return 'R64'
			case 'R31': return 'R32'
			case 'R15': return 'R16'
			case 'R7': return 'QF'
			case 'R3': return 'SF'
			case 'R1': return 'F'
			default: return round
		}
	}

	static def mapEntry(String entry) {
		if (entry) {
			if (entry.endsWith(') W'))
				'WC'
			else if (entry == 'S')
				null
		}
		else
			entry
	}

	static def mapRankPoints(String level) {
		switch (level) {
			case 'G': return 2000
			case 'F': return 1500
			case 'M': return 1000
			case 'O': return 750
			default: return null
		}
	}


	// Davis Cup

	static def DavisCupTournamentInfo extractDCTournamentInfo(String name) {
		String[] parts = name.split(' ')
		if (parts.length < 4)
			throw new IllegalArgumentException("Invalid Davis Cup tournament name: $name")
		def group = parts[2]
		def round = parts[3]
		if (round.endsWith(':'))
			round = round.substring(0, round.length() - 1)
		new DavisCupTournamentInfo(extId: 'D' + group, name: 'Davis Cup ' + group, round: mapDCRound(round, group))
	}

	static def mapDCRound(String round, String group) {
		switch (round) {
			case 'F': return 'F'
			case 'SF': return 'SF'
			case 'QF': return 'QF'
			case 'R1': return group == 'WG' ? 'R16' : 'RR'
			default: return 'RR'
		}
	}

	static def dcMatchNum(String extTourneyId, String matchNum) {
		if (extTourneyId.startsWith('D'))
			extTourneyId = extTourneyId.substring(1)
		extTourneyId + matchNum;
	}

	static class DavisCupTournamentInfo {
		String extId
		String name
		String round
	}
}
