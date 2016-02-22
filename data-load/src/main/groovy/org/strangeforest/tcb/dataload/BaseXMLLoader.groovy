package org.strangeforest.tcb.dataload

import groovy.sql.*
import org.strangeforest.tcb.util.*

import java.sql.*
import java.util.Date

abstract class BaseXMLLoader {

	protected Sql sql

	private static def CLASSPATH_PREFIX = 'classpath:'
	private static def PROGRESS_LINE_WRAP = 100

	BaseXMLLoader(Sql sql) {
		this.sql = sql
	}

	abstract int batch()
	abstract boolean loadItem(item)

	def loadFile(String file) {
		println "Loading file '$file'"
		def t0 = System.currentTimeMillis()
		def batch = batch()
		def data = new XmlSlurper().parse(getReader(file))
		def rows = 0

		for (item in data.children()) {
			if (loadItem(item)) {
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
		s?.toString() ?: d
	}

	static Integer integer(i) {
		i?.toString()?.toInteger() ?: null
	}

	static Short smallint(i) {
		i?.toString()?.toShort() ?: null
	}

	static Boolean bool(b, d = null) {
		b?.toString()?.toBoolean() ?: d
	}

	static java.sql.Date date(d) {
		d = d?.toString()
		d ? new java.sql.Date(Date.parse('yyyy-MM-dd', d).time) : null
	}

	static Array shortArray(conn, a) {
		conn.createArrayOf('smallint', a)
	}

	static String country(c) {
		c = c?.toString()
		c && CountryUtil.code(c) ? c : null
	}
}
