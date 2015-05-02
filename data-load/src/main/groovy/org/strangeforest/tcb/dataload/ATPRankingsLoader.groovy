package org.strangeforest.tcb.dataload

import groovy.sql.*

class ATPRankingsLoader extends BaseCSVLoader {

	ATPRankingsLoader(Sql sql) {
		super(sql)
	}

	List columnNames() {
		['rank_date', 'rank', 'player_id', 'rank_points']
	}

	String loadSql() {
		'{call load_atp_ranking(:rank_date, :ext_player_id, :rank, :rank_points)}'
	}

	int batch() { 500 }

	Map params(def line) {
		def params = [:]
		params.rank_date = date line.rank_date
		params.ext_player_id = integer line.player_id
		params.rank = integer line.rank
		params.rank_points = integer line.rank_points
		return params
	}
}
