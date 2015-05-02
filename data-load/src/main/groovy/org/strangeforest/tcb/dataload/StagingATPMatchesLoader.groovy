package org.strangeforest.tcb.dataload

import groovy.sql.*

class StagingATPMatchesLoader extends BaseCSVLoader {

	StagingATPMatchesLoader(Sql sql) {
		super(sql)
	}

	String loadSql() {
		'{call stage_atp_match(' +
			':tourney_id, :tourney_name, :surface, :draw_size, :tourney_level, :tourney_date, :match_num, ' +
			':winner_id, :winner_seed, :winner_entry, :winner_name, :winner_hand, :winner_ht, :winner_ioc, :winner_age, :winner_rank, :winner_rank_points, ' +
			':loser_id, :loser_seed, :loser_entry, :loser_name, :loser_hand, :loser_ht, :loser_ioc, :loser_age, :loser_rank, :loser_rank_points, ' +
			':score, :best_of, :round, :minutes, ' +
			':w_ace, :w_df, :w_svpt, :w_1stIn, :w_1stWon, :w_2ndWon, :w_SvGms, :w_bpSaved, :w_bpFaced, ' +
			':l_ace, :l_df, :l_svpt, :l_1stIn, :l_1stWon, :l_2ndWon, :l_SvGms, :l_bpSaved, :l_bpFaced' +
		')}'
	}

	int batch() { 100 }

	Map params(def line) {
		def params = [:]
		params.tourney_id = line.tourney_id
		params.tourney_name = line.tourney_name
		params.surface = line.surface
		params.draw_size = integer line.draw_size
		params.tourney_level = line.tourney_level
		params.tourney_date = date line.tourney_date
		params.match_num = integer line.match_num
		params.winner_id = integer line.winner_id
		params.winner_seed = integer line.winner_seed
		params.winner_entry = line.winner_entry
		params.winner_name = line.winner_name
		params.winner_hand = line.winner_hand
		params.winner_ht = integer line.winner_ht
		params.winner_ioc = line.winner_ioc
		params.winner_age = decimal line.winner_age
		params.winner_rank = integer line.winner_rank
		params.winner_rank_points = integer line.winner_rank_points
		params.loser_id = integer line.loser_id
		params.loser_seed = integer line.loser_seed
		params.loser_entry = line.loser_entry
		params.loser_name = line.loser_name
		params.loser_hand = line.loser_hand
		params.loser_ht = integer line.loser_ht
		params.loser_ioc = line.loser_ioc
		params.loser_age = decimal line.loser_age
		params.loser_rank = integer line.loser_rank
		params.loser_rank_points = integer line.loser_rank_points
		params.score = line.score
		params.best_of = integer line.best_of
		params.round = line.round
		params.minutes = integer line.minutes
		params.w_ace = integer line.w_ace
		params.w_df = integer line.w_df
		params.w_svpt = integer line.w_svpt
		params.w_1stIn = integer line.w_1stIn
		params.w_1stWon = integer line.w_1stWon
		params.w_2ndWon = integer line.w_2ndWon
		params.w_SvGms = integer line.w_SvGms
		params.w_bpSaved = integer line.w_bpSaved
		params.w_bpFaced = integer line.w_bpFaced
		params.l_ace = integer line.l_ace
		params.l_df = integer line.l_df
		params.l_svpt = integer line.l_svpt
		params.l_1stIn = integer line.l_1stIn
		params.l_1stWon = integer line.l_1stWon
		params.l_2ndWon = integer line.l_2ndWon
		params.l_SvGms = integer line.l_SvGms
		params.l_bpSaved = integer line.l_bpSaved
		params.l_bpFaced = integer line.l_bpFaced
		return params
	}
}
