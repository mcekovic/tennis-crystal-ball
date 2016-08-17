package org.strangeforest.tcb.dataload

import java.sql.*

class StagingPlayerLoader extends BaseCSVLoader {

	StagingPlayerLoader(SqlPool sqlPool) {
		super(sqlPool)
	}

	List columnNames() {
		['player_id', 'first_name', 'last_name', 'hand', 'dob', 'country']
	}

	String loadSql() {
		'{call stage_player(:player_id, :first_name, :last_name, :hand, :dob, :country)}'
	}

	int batchSize() { 500 }

	Map params(def record, Connection conn) {
		def params = [:]
		params.player_id = integer record.player_id
		params.first_name = record.first_name
		params.last_name = record.last_name
		params.hand = record.hand
		params.dob = date record.dob
		params.country = record.country
		return params
	}
}
