package org.strangeforest.tcb.dataload

import groovy.sql.*

class StagingATPPlayersLoader extends BaseCSVLoader {

	StagingATPPlayersLoader(Sql sql) {
		super(sql)
	}

	List columnNames() {
		['player_id', 'first_name', 'last_name', 'hand', 'dob', 'country']
	}

	String loadSql() {
		'{call stage_atp_player(:player_id, :first_name, :last_name, :hand, :dob, :country)}'
	}

	int batch() { 500 }

	Map params(def line) {
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
