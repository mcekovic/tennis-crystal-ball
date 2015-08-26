package org.strangeforest.tcb.dataload

import groovy.sql.*

class AdditionalPlayerDataLoader extends BaseXMLLoader {

	AdditionalPlayerDataLoader(Sql sql) {
		super(sql)
	}

	String loadSql(def item) {
		def children = item.children()
		if (children) {
			def sql = "UPDATE player SET"
			def index = 0
			for (attr in children) {
				def attrName = attr.name()
				def column = columnMap[attrName]
				if (index++)
					sql += ','
				sql += " $column = :$attrName"
			}
			sql + " WHERE first_name || ' ' || last_name = :name"
		}
	}

	int batch() { 100 }

	Map params(def item) {
		def params = [:]
		params.name = string item.@'name'
		for (attr in item.children())
			params[attr.name()] = attr.text()
		return params
	}

	static def columnMap = [
		'dob': 'dob',
		'birthplace': 'birthplace',
		'residence': 'residence',
		'height': 'height',
		'weight': 'weight',
		'hand': 'hand',
		'backhand': 'backhand',
		'turnedPro': 'turned_pro',
		'coach': 'coach',
		'webSite': 'web_site',
		'twitter': 'twitter',
		'facebook': 'facebook'
	]
}
