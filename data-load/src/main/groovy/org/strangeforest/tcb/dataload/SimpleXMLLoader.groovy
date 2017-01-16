package org.strangeforest.tcb.dataload

import groovy.sql.*

abstract class SimpleXMLLoader extends BaseXMLLoader {

	SimpleXMLLoader(Sql sql) {
		super(sql)
	}

	abstract String loadSql(item)
	abstract Map params(item)
	abstract String toString(item)

	boolean loadItem(item) {
		def loadSql = loadSql(item)
		if (!loadSql)
			false
		int updated = sql.executeUpdate(params(item), loadSql)
		if (!updated)
			throw new NoSuchElementException('Cannot find ' + toString(item))
		true
	}
}
