package org.strangeforest.tcb.dataload

import groovy.sql.*

import java.util.concurrent.*

class StagingRankingLoader extends BaseCSVLoader {

	StagingRankingLoader(BlockingDeque<Sql> sqlPool) {
		super(sqlPool)
	}

	List columnNames() {
		['rank_date', 'rank', 'player_id', 'rank_points']
	}

	String loadSql() {
		'{call stage_ranking(:rank_date, :rank, :player_id, :rank_points)}'
	}

	int batchSize() { 500 }

	Map params(record, conn) {
		def params = [:]
		params.rank_date = date record.rank_date
		params.rank = integer record.rank
		params.player_id = integer record.player_id
		params.rank_points = integer record.rank_points
		return params
	}
}
