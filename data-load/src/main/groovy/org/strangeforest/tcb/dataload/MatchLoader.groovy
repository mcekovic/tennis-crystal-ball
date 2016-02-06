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
			':ext_tournament_id, :season, :tournament_date, :tournament_name, :event_name, :tournament_level, :surface, :indoor, :draw_type, :draw_size, :rank_points, ' +
			':match_num, :round, :best_of, ' +
			':ext_winner_id, :winner_seed, :winner_entry, :winner_rank, :winner_rank_points, :winner_age, :winner_country_id, :winner_name, :winner_height, :winner_hand, ' +
			':ext_loser_id, :loser_seed, :loser_entry, :loser_rank, :loser_rank_points, :loser_age, :loser_country_id, :loser_name, :loser_height, :loser_hand, ' +
			':score, :outcome, :w_sets, :l_sets, :w_games, :l_games, :w_set_games, :l_set_games, :w_set_tb_pt, :l_set_tb_pt, :minutes, ' +
			':w_ace, :w_df, :w_sv_pt, :w_1st_in, :w_1st_won, :w_2nd_won, :w_sv_gms, :w_bp_sv, :w_bp_fc, ' +
			':l_ace, :l_df, :l_sv_pt, :l_1st_in, :l_1st_won, :l_2nd_won, :l_sv_gms, :l_bp_sv, :l_bp_fc' +
		')}'
	}

	int batchSize() { 100 }

	Map params(record, conn) {
		def params = [:]

		def tourneyId = string record.tourney_id
		if (tourneyId[4] != '-')
			throw new IllegalArgumentException("Invalid tourney_id: $tourneyId")
		def extTourneyId = string(tourneyId.substring(5))
		def level = string record.tourney_level
		def name = string record.tourney_name
		def dcInfo = level == 'D' ? extractDCTournamentInfo(name) : null;
		def season = smallint(tourneyId.substring(0, 4))
		params.season = season
		params.tournament_date = date record.tourney_date

		def drawSize = smallint record.draw_size
		def mappedLevel = mapLevel(level, drawSize, name, season, extTourneyId)
		params.ext_tournament_id = mapExtTournamentId(extTourneyId, mappedLevel, dcInfo)
		def eventName = mappedLevel != 'D' ? name : dcInfo.name
		params.tournament_name = mappedLevel != 'O' ? eventName : 'Olympics'
		params.event_name = eventName
		params.tournament_level = mappedLevel
		def surface = string record.surface
		params.surface = mapSurface surface
		params.indoor = mapIndoor surface
		params.draw_type = mappedLevel != 'F' ? 'KO' : 'RR';
		params.draw_size = drawSize
		params.rank_points = mapRankPoints mappedLevel

		def matchNum = record.match_num
		params.match_num = level != 'D' ? smallint(matchNum) : smallint(dcMatchNum(extTourneyId, matchNum))
		def round = string record.round
		params.round = level != 'D' ? mapRound(round) : dcInfo.round
		params.best_of = smallint record.best_of

		params.ext_winner_id = integer record.winner_id
		params.winner_seed = smallint record.winner_seed
		params.winner_entry = mapEntry(string(record.winner_entry))
		params.winner_rank = integer record.winner_rank
		params.winner_rank_points = integer record.winner_rank_points
		params.winner_age = real record.winner_age
		params.winner_country_id = country record.winner_ioc
		params.winner_name = string record.winner_name
		params.winner_height = smallint record.winner_ht
		params.winner_hand = hand record.winner_hand
		
		params.ext_loser_id = integer record.loser_id
		params.loser_seed = smallint record.loser_seed
		params.loser_entry = mapEntry(string(record.loser_entry))
		params.loser_rank = integer record.loser_rank
		params.loser_rank_points = integer record.loser_rank_points
		params.loser_age = real record.loser_age
		params.loser_country_id = country record.loser_ioc
		params.loser_name = string record.loser_name
		params.loser_height = smallint record.loser_ht
		params.loser_hand = hand record.loser_hand

		def score = record.score.trim()
		def matchScore = MatchScore.parse(score)
		params.score = string score
		params.outcome = matchScore?.outcome
		params.w_sets = matchScore?.w_sets
		params.l_sets = matchScore?.l_sets
		params.w_games = matchScore?.w_games
		params.l_games = matchScore?.l_games
		params.w_set_games = matchScore ? shortArray(conn, matchScore.w_set_games) : null
		params.l_set_games = matchScore ? shortArray(conn, matchScore.l_set_games) : null
		params.w_set_tb_pt = matchScore ? shortArray(conn, matchScore.w_set_tb_pt) : null
		params.l_set_tb_pt = matchScore ? shortArray(conn, matchScore.l_set_tb_pt) : null

		params.minutes = smallint record.minutes

		params.w_ace = smallint record.w_ace
		params.w_df = smallint record.w_df
		params.w_sv_pt = smallint record.w_svpt
		params.w_1st_in = smallint record.w_1stIn
		params.w_1st_won = smallint record.w_1stWon
		params.w_2nd_won = smallint record.w_2ndWon
		params.w_sv_gms = smallint record.w_SvGms
		params.w_bp_sv = smallint record.w_bpSaved
		params.w_bp_fc = smallint record.w_bpFaced

		params.l_ace = smallint record.l_ace
		params.l_df = smallint record.l_df
		params.l_sv_pt = smallint record.l_svpt
		params.l_1st_in = smallint record.l_1stIn
		params.l_1st_won = smallint record.l_1stWon
		params.l_2nd_won = smallint record.l_2ndWon
		params.l_sv_gms = smallint record.l_SvGms
		params.l_bp_sv = smallint record.l_bpSaved
		params.l_bp_fc = smallint record.l_bpFaced
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
			case 'M':
				if (name.startsWith('Masters') && drawSize <= 16)
					return 'F'
				else if (
					(name.equals('Cincinnati') && (1968..1980).contains(season)) ||
					(name.equals('Delray Beach') && season == 1985) ||
					(name.equals('Montreal / Toronto') && ((1969..1971).contains(season) || (1976..1977).contains(season))) ||
					(name.equals('Rome') && (1968..1969).contains(season))
				)
					return 'A'
				else
					return 'M'
			case 'A':
				if (name.contains('Olympics'))
					return 'O'
				else if (name.startsWith('Australian Open') && season == 1977)
					return 'G'
				else if (
					(name.startsWith('Boston') && (1970..1977).contains(season)) ||
					(name.startsWith('Forest Hills') && (1982..1985).contains(season)) ||
					(name.equals('Hamburg') && (1978..1989).contains(season)) ||
					(name.equals('Indianapolis') && (1974..1977).contains(season)) ||
					(name.equals('Johannesburg') && (1972..1974).contains(season)) ||
					(name.startsWith('Las Vegas') && (1972..1981).contains(season) && drawSize >= 32) ||
					(name.startsWith('Los Angeles') && (1970..1973).contains(season) && drawSize >= 64) ||
					(name.startsWith('Monte Carlo') && (1970..1989).contains(season)) ||
					(name.startsWith('Monte-Carlo') && season == 2015) ||
					(name.startsWith('Paris') && [1989, 2015].contains(season)) ||
					(name.startsWith('Philadelphia') && (1970..1986).contains(season)) ||
					(name.equals('Shanghai') && season == 2015) ||
					(name.equals('Sydney Outdoor') && season == 1971) ||
					(name.equals('Stockholm') && ((1972..1980).contains(season) || (1984..1989).contains(season))) ||
					(name.equals('Stockholm Open') && (1970..1971).contains(season)) ||
					(name.equals('Tokyo Indoor') && (1978..1988).contains(season)) ||
					(name.startsWith('Washington') && extTournamentId.equals('418') && (1975..1977).contains(season)) ||
					(name.equals('Wembley') && ((1970..1971).contains(season) || (1976..1983).contains(season)))
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
				return 'WC'
			else if (entry == 'S')
				return null
		}
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

	static def SAME_TOURNAMENT_MAP = [
		'2013': '393',
		'2041': '393',
		'6116': '409',
		'M020': '339',
		'0891': '891',
		'0451': '451',
		'3937': '301',
		'1720': '334',
		'3944': '316',
		'712': '650',
		'725': '650',
		'6718': '359',
		'417': '80',
		'3943': '80',
		'3938': '347',
		'3942': '313',
		'506': '303',
		'3939': '344',
		'2063': '360',
		'803': '499',
		'805': '437',
		'661': '615',
		'6710': '615',
		'7290': '468',
		'820': '405',
		'7163': '2027',
		'5012': '426',
	]
}
