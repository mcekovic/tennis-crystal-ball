package org.strangeforest.tcb.dataload

import java.sql.*

class StagingMatchLoader extends BaseCSVLoader {

	StagingMatchLoader(SqlPool sqlPool) {
		super(sqlPool)
	}

	String loadSql() {
		'{call stage_match(' +
			':tourney_id, :tourney_name, :surface, :draw_size, :tourney_level, :tourney_date, :match_num, ' +
			':winner_id, :winner_seed, :winner_entry, :winner_name, :winner_hand, :winner_ht, :winner_ioc, :winner_age, :winner_rank, :winner_rank_points, ' +
			':loser_id, :loser_seed, :loser_entry, :loser_name, :loser_hand, :loser_ht, :loser_ioc, :loser_age, :loser_rank, :loser_rank_points, ' +
			':score, :best_of, :round, :minutes, ' +
			':w_ace, :w_df, :w_svpt, :w_1stIn, :w_1stWon, :w_2ndWon, :w_SvGms, :w_bpSaved, :w_bpFaced, ' +
			':l_ace, :l_df, :l_svpt, :l_1stIn, :l_1stWon, :l_2ndWon, :l_SvGms, :l_bpSaved, :l_bpFaced' +
		')}'
	}

	int batchSize() { 100 }

	Map params(record, Connection conn) {
		def params = [:]
		params.tourney_id = record.tourney_id
		params.tourney_name = record.tourney_name
		params.surface = record.surface
		params.draw_size = integer record.draw_size
		params.tourney_level = record.tourney_level
		params.tourney_date = date record.tourney_date
		params.match_num = integer record.match_num
		params.winner_id = integer record.winner_id
		params.winner_seed = integer record.winner_seed
		params.winner_entry = record.winner_entry
		params.winner_name = record.winner_name
		params.winner_hand = record.winner_hand
		params.winner_ht = integer record.winner_ht
		params.winner_ioc = record.winner_ioc
		params.winner_age = decimal record.winner_age
		params.winner_rank = integer record.winner_rank
		params.winner_rank_points = integer record.winner_rank_points
		params.loser_id = integer record.loser_id
		params.loser_seed = integer record.loser_seed
		params.loser_entry = record.loser_entry
		params.loser_name = record.loser_name
		params.loser_hand = record.loser_hand
		params.loser_ht = integer record.loser_ht
		params.loser_ioc = record.loser_ioc
		params.loser_age = decimal record.loser_age
		params.loser_rank = integer record.loser_rank
		params.loser_rank_points = integer record.loser_rank_points
		params.score = record.score
		params.best_of = integer record.best_of
		params.round = record.round
		params.minutes = integer record.minutes
		params.w_ace = integer record.w_ace
		params.w_df = integer record.w_df
		params.w_svpt = integer record.w_svpt
		params.w_1stIn = integer record.w_1stIn
		params.w_1stWon = integer record.w_1stWon
		params.w_2ndWon = integer record.w_2ndWon
		params.w_SvGms = integer record.w_SvGms
		params.w_bpSaved = integer record.w_bpSaved
		params.w_bpFaced = integer record.w_bpFaced
		params.l_ace = integer record.l_ace
		params.l_df = integer record.l_df
		params.l_svpt = integer record.l_svpt
		params.l_1stIn = integer record.l_1stIn
		params.l_1stWon = integer record.l_1stWon
		params.l_2ndWon = integer record.l_2ndWon
		params.l_SvGms = integer record.l_SvGms
		params.l_bpSaved = integer record.l_bpSaved
		params.l_bpFaced = integer record.l_bpFaced
		return params
	}
}
