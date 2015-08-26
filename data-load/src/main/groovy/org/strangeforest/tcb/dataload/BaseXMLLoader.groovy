package org.strangeforest.tcb.dataload

import groovy.sql.*

abstract class BaseXMLLoader {

	private Sql sql

	private static def CLASSPATH_PREFIX = 'classpath:'
	private static def PROGRESS_LINE_WRAP = 100

	BaseXMLLoader(Sql sql) {
		this.sql = sql
	}

	abstract String loadSql(def item)
	abstract int batch()
	abstract Map params(def item)
	abstract String toString(def item)

	def loadFile(String file) {
		println "Loading file '$file'"
		def t0 = System.currentTimeMillis()
		def batch = batch()
		def data = new XmlSlurper().parse(getReader(file))
		def rows = 0

		for (item in data.children()) {
			def loadSql = loadSql(item)
			if (loadSql) {
				int updated = sql.executeUpdate(params(item), loadSql)
				if (!updated)
					throw new NoSuchElementException('Cannot find ' + toString(item))
				if (++rows % batch == 0) {
					sql.commit()
					print '.'
					if (rows % (batch * PROGRESS_LINE_WRAP) == 0)
						println()
				}
			}
		}
		sql.commit()
		println ''
		def seconds = (System.currentTimeMillis() - t0) / 1000.0
		int rowsPerSecond = rows/seconds
		println "Rows: $rows in $seconds s ($rowsPerSecond row/s)"
		return rows
	}

	private def getReader(String file) {
		if (file.startsWith(CLASSPATH_PREFIX)) {
			def path = file.substring(CLASSPATH_PREFIX.length())
			def stream = getClass().getResourceAsStream(path)
			if (stream)
				new InputStreamReader(stream)
			else
				throw new FileNotFoundException("Cannot find file '$path' in classpath.")
		}
		else
			new FileReader(file)
	}


	// Data conversion

	static String string(s, d = null) {
		s ? s : d
	}

	static Integer integer(i) {
		i ? i.toInteger() : null
	}

	static java.sql.Date date(d) {
		d ? new java.sql.Date(Date.parse('dd-MM-yyyy', d).time) : null
	}
}
