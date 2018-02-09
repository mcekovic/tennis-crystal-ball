package org.strangeforest.tcb.dataload

import groovy.sql.*

class AdditionalPlayerDataLoader extends SimpleXMLLoader {

	AdditionalPlayerDataLoader(Sql sql) {
		super(sql)
	}

	String loadSql(item) {
		def children = item.children()
		if (!children.isEmpty()) {
			def sql = 'UPDATE player SET'
			def index = 0
			for (attr in children) {
				def attrName = attr.name()
				def column = column attrName
				def attrCast = cast column
				if (index++)
					sql += ','
				sql += " $column = :$attrCast"
			}
			sql + ' WHERE lower(full_name(first_name, last_name)) = lower(:name) OR lower(full_name(first_name, last_name)) = (SELECT lower(alias) FROM player_alias WHERE lower(name) = lower(:name))'
		}
		else
			null
	}

	static column(attr) {
		def column = COLUMN_MAP[attr]
		column ?: attr
	}

	static cast(attr) {
		def cast = CAST_MAP[attr]
		cast ? attr + '::' + cast : attr
	}

	int batch() { 100 }

	Map params(item) {
		def params = [:]
		params.name = string item.@name
		params.dob = date item.dob
		params.birthplace = string item.birthplace
		params.residence = string item.residence
		params.height = integer item.height
		params.weight = integer item.weight
		params.hand = string item.hand
		params.backhand = string item.backhand
		params.turned_pro = integer item.'turned-pro'
		params.coach = string item.coach
		params.web_site = string item.'web-site'
		params.facebook = string item.facebook
		params.twitter = string item.twitter
		return params
	}

	String toString(item) {
		'player ' + string (item.@name)
	}

	private static COLUMN_MAP = [
		'turned-pro': 'turned_pro',
		'web-site': 'web_site'
	]

	private static CAST_MAP = [
		'hand': 'player_hand',
		'backhand': 'player_backhand'
	]
}
