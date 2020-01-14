package org.strangeforest.tcb.dataload

import java.sql.*

class StagingRankingLoader extends BaseCSVLoader {

	StagingRankingLoader(SqlPool sqlPool) {
		super(sqlPool)
	}

	List columnNames() {
		['rank_date', 'rank', 'player_id', 'rank_points']
	}

	String loadSql() {
		'CALL stage_ranking(:rank_date, :rank, :player_id, :rank_points)'
	}

	int batchSize() { 500 }

	Map params(record, Connection conn) {
		def params = [:]
		params.rank_date = date record.rank_date
		params.rank = integer record.rank
		params.player_id = integer record.player_id
		params.rank_points = integer record.rank_points
		return params
	}
}
