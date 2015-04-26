package org.strangeforest.tcb.dataload

import groovy.sql.*

class ATPRankingsLoader extends BaseLoader {

	def PARAMS = '(:rank_date, :rank, :player_id, :rank_points)'

	ATPRankingsLoader(Sql sql) {
		super(sql)
	}

	List columnNames() {
		['rank_date', 'rank', 'player_id', 'rank_points']
	}

	def insertSql() {
		'INSERT INTO atp_rankings (rank_date, rank, player_id, rank_points) VALUES ' + PARAMS
	}

	def mergeSql() {
		"{call merge_atp_ranking$PARAMS}"
	}

	def batch() { 500 }

	def params(def line) {
		def params = [:]
		params.rank_date = date line.rank_date
		params.rank = integer line.rank
		params.player_id = integer line.player_id
		params.rank_points = integer line.rank_points
		return params
	}
}
