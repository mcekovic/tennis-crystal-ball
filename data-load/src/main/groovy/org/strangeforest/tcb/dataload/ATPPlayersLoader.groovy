package org.strangeforest.tcb.dataload

import groovy.sql.*

class ATPPlayersLoader extends BaseLoader {

	def PARAMS = '(:player_id, :first_name, :last_name, :hand, :dob, :country)'

	ATPPlayersLoader(Sql sql) {
		super(sql)
	}

	List columnNames() {
		['player_id', 'first_name', 'last_name', 'hand', 'dob', 'country']
	}

	def insertSql() {
		'INSERT INTO atp_players (player_id, first_name, last_name, hand, dob, country) VALUES ' + PARAMS
	}

	def mergeSql() {
		"{call merge_atp_player$PARAMS}"
	}

	def batch() { 500 }

	def params(def line) {
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
