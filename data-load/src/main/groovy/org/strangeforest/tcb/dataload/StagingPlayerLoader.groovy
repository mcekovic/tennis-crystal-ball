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

	Map params(line, sql) {
		def params = [:]
		params.player_id = integer line.player_id
		params.first_name = line.first_name
		params.last_name = line.last_name
		params.hand = line.hand
		params.dob = date line.dob
		params.country = line.country
		return params
	}
}
