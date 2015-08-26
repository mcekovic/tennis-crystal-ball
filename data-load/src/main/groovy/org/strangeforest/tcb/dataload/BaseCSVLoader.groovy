package org.strangeforest.tcb.dataload

import org.strangeforest.tcb.util.CountryUtil

import java.sql.*
import java.util.Date

import com.xlson.groovycsv.*
import groovy.sql.*

abstract class BaseCSVLoader {

	private Sql sql

	private static def PROGRESS_LINE_WRAP = 100

	BaseCSVLoader(Sql sql) {
		this.sql = sql
	}

	List columnNames() { null }
	abstract String loadSql()
	abstract int batch()
	abstract Map params(def line)

	def loadFile(String file) {
		println "Loading file '$file'"
		def t0 = System.currentTimeMillis()
		List columnNames = columnNames()
		def loadSql = loadSql()
		def batch = batch()
		def csvParams = columnNames ? [columnNames: columnNames, readFirstLine: true] : [:]
		def data = CsvParser.parseCsv(csvParams, new FileReader(file))
		def rows = 0
		for (line in data) {
			sql.execute(params(line), loadSql)
			if (++rows % batch == 0) {
				sql.commit()
				print '.'
				if (rows % (batch * PROGRESS_LINE_WRAP) == 0)
					println()
			}
		}
		sql.commit()
		println ''
		def seconds = (System.currentTimeMillis() - t0) / 1000.0
		int rowsPerSecond = rows/seconds
		println "Rows: $rows in $seconds s ($rowsPerSecond row/s)"
		return rows
	}


	// Data conversion

	static String string(s, d = null) {
		s ? s : d
	}

	static Integer integer(i) {
		i ? i.toInteger() : null
	}

	static Short smallint(i) {
		i ? i.toShort() : null
	}

	static BigDecimal decimal(d) {
		d ? d.toBigDecimal() : null
	}

	static Float real(f) {
		f ? f.toFloat() : null
	}

	static java.sql.Date date(d) {
		if (d) {
			switch (d.length()) {
				case 4: d += '0701'; break
				case 6: d += '15'; break
			}
			new java.sql.Date(Date.parse('yyyyMMdd', d).time)
		}
		else
			null
	}

	Array shortArray(a) {
		sql.connection.createArrayOf('smallint', a)
	}

	static String country(c) {
		c && CountryUtil.code(c) ? c : CountryUtil.UNKNOWN
	}

	static String hand(c) {
		switch (c) {
			case 'R': return 'R'
			case 'L': return 'L'
			default: return null
		}
	}
}
