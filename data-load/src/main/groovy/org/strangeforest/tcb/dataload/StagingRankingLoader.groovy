package org.strangeforest.tcb.dataload

import groovy.sql.*

class StagingRankingLoader extends BaseCSVLoader {

	StagingRankingLoader(Sql sql) {
		super(sql)
	}

	List columnNames() {
		['rank_date', 'rank', 'player_id', 'rank_points']
	}

	String loadSql() {
		'{call stage_ranking(:rank_date, :rank, :player_id, :rank_points)}'
	}

	int batchSize() { 500 }

	Map params(def line) {
		def params = [:]
		params.rank_date = date line.rank_date
		params.rank = integer line.rank
		params.player_id = integer line.player_id
		params.rank_points = integer line.rank_points
		return params
	}
}
