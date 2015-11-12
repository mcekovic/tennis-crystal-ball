package org.strangeforest.tcb.dataload

import groovy.sql.*

import java.util.concurrent.*

class StagingPlayerLoader extends BaseCSVLoader {

	StagingPlayerLoader(BlockingDeque<Sql> sqlPool) {
		super(sqlPool)
	}

	List columnNames() {
		['player_id', 'first_name', 'last_name', 'hand', 'dob', 'country']
	}

	String loadSql() {
		'{call stage_player(:player_id, :first_name, :last_name, :hand, :dob, :country)}'
	}

	int batchSize() { 500 }

	Map params(record, conn) {
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
