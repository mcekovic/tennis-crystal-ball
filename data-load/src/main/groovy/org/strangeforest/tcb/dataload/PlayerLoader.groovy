package org.strangeforest.tcb.dataload

import groovy.sql.*
import org.strangeforest.tcb.util.*

import java.util.concurrent.*

class PlayerLoader extends BaseCSVLoader {

	PlayerLoader(BlockingDeque<Sql> sqlPool) {
		super(sqlPool)
	}

	List columnNames() {
		['player_id', 'first_name', 'last_name', 'hand', 'dob', 'country']
	}

	String loadSql() {
		'{call load_player(:ext_player_id, :first_name, :last_name, :dob, :country_id, :hand)}'
	}

	int batchSize() { 500 }

	Map params(record, conn) {
		def params = [:]
		params.ext_player_id = integer record.player_id
		params.first_name = string record.first_name
		params.last_name = string record.last_name
		params.dob = date record.dob
		params.country_id = country record.country, CountryUtil.UNKNOWN
		params.hand = hand record.hand
		return params
	}
}
