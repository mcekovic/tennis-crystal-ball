package org.strangeforest.tcb.dataload

import java.sql.*

import static org.strangeforest.tcb.dataload.LoadParams.*

class RankingLoader extends BaseCSVLoader {

	Integer maxRank

	RankingLoader(SqlPool sqlPool) {
		super(sqlPool)
		maxRank = getIntProperty(MAX_RANK_PROPERTY)
	}

	List columnNames() {
		['rank_date', 'rank', 'player_id', 'rank_points']
	}

	String loadSql() {
		'CALL load_ranking(?, ?, ?, ?)'
	}

	int batchSize() { 1000 }

	List params(record, Connection conn) {
		def rank = integer record.rank
		if (maxRank && rank > maxRank)
			return null
		[date(record.rank_date), integer(record.player_id), rank, integer(record.rank_points)]
	}
}
