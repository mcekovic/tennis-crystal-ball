package org.strangeforest.tcb.dataload

import groovy.sql.*

class MatchLoader extends BaseCSVLoader {

	MatchLoader(Sql sql) {
		super(sql)
	}

	String loadSql() {
		'{call load_match(' +
			':ext_tournament_id, :season, :tournament_date, :tournament_name, :tournament_level, :surface, :indoor, :draw_size, :rank_points, ' +
			':match_num, :round, :best_of, ' +
			':ext_winner_id, :winner_seed, :winner_entry, :winner_rank, :winner_rank_points, :winner_age, :winner_country_id, :winner_name, :winner_height, :winner_hand, ' +
			':ext_loser_id, :loser_seed, :loser_entry, :loser_rank, :loser_rank_points, :loser_age, :loser_country_id, :loser_name, :loser_height, :loser_hand, ' +
			':score, :minutes, ' +
			':w_ace, :w_df, :w_sv_pt, :w_1st_in, :w_1st_won, :w_2nd_won, :w_sv_gms, :w_bp_sv, :w_bp_fc, ' +
			':l_ace, :l_df, :l_sv_pt, :l_1st_in, :l_1st_won, :l_2nd_won, :l_sv_gms, :l_bp_sv, :l_bp_fc' +
		')}'
	}

	int batch() { 100 }

	Map params(def line) {
		def params = [:]

		def tourneyId = string line.tourney_id
		if (tourneyId[4] != '-')
			throw new IllegalArgumentException("Invalid tourney_id: $tourneyId")
		params.ext_tournament_id = string tourneyId.substring(5)
		params.season = smallint tourneyId.substring(0, 4)
		params.tournament_date = date line.tourney_date

		def name = string line.tourney_name
		params.tournament_name = name
		def level = string line.tourney_level
		def drawSize = smallint line.draw_size
		def mappedLevel = mapLevel(level, drawSize, name)
		params.tournament_level = mappedLevel
		def surface = string line.surface
		params.surface = mapSurface surface
		params.indoor = mapIndoor surface
		params.draw_size = drawSize
		params.rank_points = mapRankPoints mappedLevel

		params.match_num = smallint line.match_num
		def round = string line.round
		params.round = mapRound round
		params.best_of = smallint line.best_of

		params.ext_winner_id = integer line.winner_id
		params.winner_seed = smallint line.winner_seed
		params.winner_entry = string line.winner_entry
		params.winner_rank = integer line.winner_rank
		params.winner_rank_points = integer line.winner_rank_points
		params.winner_age = real line.winner_age
		params.winner_country_id = string line.winner_ioc
		params.winner_name = string line.winner_name
		params.winner_height = smallint line.winner_ht
		params.winner_hand = hand line.winner_hand
		
		params.ext_loser_id = integer line.loser_id
		params.loser_seed = smallint line.loser_seed
		params.loser_entry = string line.loser_entry
		params.loser_rank = integer line.loser_rank
		params.loser_rank_points = integer line.loser_rank_points
		params.loser_age = real line.loser_age
		params.loser_country_id = string line.loser_ioc
		params.loser_name = string line.loser_name
		params.loser_height = smallint line.loser_ht
		params.loser_hand = hand line.loser_hand

		def score = line.score
		def matchScore = MatchScore.parse(score)
		params.score = string score
		params.outcome = matchScore?.outcome
		params.w_sets = matchScore?.w_sets
		params.l_sets = matchScore?.l_sets
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

	static def mapLevel(String level, short drawSize, String name) {
		switch (level) {
			case 'G': return 'G'
			case 'F': return 'F'
			case 'M': return level.startsWith('Masters') && drawSize <= 16 ? 'F' : 'M'
			case 'A': return name.contains('Olympics') ? 'O' : 'A'
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

	static def mapRankPoints(String level) {
		switch (level) {
			case 'G': return 2000
			case 'F': return 1500
			case 'M': return 1000
			case 'O': return 750
			default: return null
		}
	}
}
