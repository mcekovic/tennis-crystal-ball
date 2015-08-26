package org.strangeforest.tcb.dataload

import groovy.sql.*

class AdditionalPlayerDataLoader extends BaseXMLLoader {

	AdditionalPlayerDataLoader(Sql sql) {
		super(sql)
	}

	String loadSql(def item) {
		def children = item.children()
		if (!children.isEmpty()) {
			def sql = "UPDATE player SET"
			def index = 0
			for (attr in children) {
				def attrName = attr.name()
				def column = column(attrName)
				def attrCast = cast(attrName)
				if (index++)
					sql += ','
				sql += " $column = :$attrCast"
			}
			sql + " WHERE first_name || ' ' || last_name = :name"
		}
		else
			null
	}

	static def column(attr) {
		def column = COLUMN_MAP[attr]
		column ? column : attr
	}

	static def cast(attr) {
		def cast = CAST_MAP[attr]
		cast ? attr + '::' + cast : attr
	}

	int batch() { 100 }

	Map params(def item) {
		def params = [:]
		params.name = string item.@'name'
		addDateParam item, params, 'dob'
		addStringParam item, params, 'birthplace'
		addStringParam item, params, 'residence'
		addIntegerParam item, params, 'height'
		addIntegerParam item, params, 'weight'
		addStringParam item, params, 'hand'
		addStringParam item, params, 'backhand'
		addIntegerParam item, params, 'turnedPro'
		addStringParam item, params, 'coach'
		addStringParam item, params, 'webSite'
		addStringParam item, params, 'twitter'
		addStringParam item, params, 'facebook'
		return params
	}

	String toString(def item) {
		'player ' + string(item.@'name')
	}

	static def addStringParam(def item, def params, def attr) {
		def node = item[attr]
		if (node)
			params[attr] = string node.text()
	}

	static def addIntegerParam(def item, def params, def attr) {
		def node = item[attr]
		if (node)
			params[attr] = integer node.text()
	}

	static def addDateParam(def item, def params, def attr) {
		def node = item[attr]
		if (node)
			params[attr] = date node.text()
	}

	private static def COLUMN_MAP = [
		'turnedPro': 'turned_pro',
		'webSite': 'web_site'
	]

	private static def CAST_MAP = [
		'hand': 'player_hand',
		'backhand': 'player_backhand'
	]
}
