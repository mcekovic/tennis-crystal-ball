package org.strangeforest.tcb.dataload

import groovy.sql.*

class PlayerLoader extends BaseCSVLoader {

	PlayerLoader(Sql sql) {
		super(sql)
	}

	List columnNames() {
		['player_id', 'first_name', 'last_name', 'hand', 'dob', 'country']
	}

	String loadSql() {
		'{call load_player(:ext_player_id, :first_name, :last_name, :dob, :country_id, :hand)}'
	}

	int batch() { 500 }

	Map params(def line) {
		def params = [:]
		params.ext_player_id = integer line.player_id
		params.first_name = string line.first_name
		params.last_name = string line.last_name
		params.dob = date line.dob
		params.country_id = country line.country
		params.hand = hand line.hand
		return params
	}
}
