package org.strangeforest.tcb.dataload

import java.sql.*

import static org.strangeforest.tcb.dataload.LoadParams.*

class RankingLoader extends BaseCSVLoader {

	Integer maxRank

	RankingLoader(SqlPool sqlPool) {
		super(sqlPool)
		maxRank = maxRank()
	}

	List columnNames() {
		['rank_date', 'rank', 'player_id', 'rank_points']
	}

	String loadSql() {
		'{call load_ranking(:rank_date, :ext_player_id, :rank, :rank_points)}'
	}

	int batchSize() { 500 }

	Map params(record, Connection conn) {
		def rank = integer record.rank
		if (maxRank && rank > maxRank)
			return null
		def params = [:]
		params.rank_date = date record.rank_date
		params.ext_player_id = integer record.player_id
		params.rank = rank
		params.rank_points = integer record.rank_points
		return params
	}

	static Integer maxRank() {
		def value = System.getProperty(MAX_RANK_PROPERTY)
		value ? Integer.parseInt(value) : null
	}
}
