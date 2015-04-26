package org.strangeforest.tcb.dataload

import com.xlson.groovycsv.*
import groovy.sql.*

import java.text.ParseException

abstract class BaseLoader {

	private Sql sql

	private def PROGRESS_LINE_WRAP = 100

	BaseLoader(Sql sql) {
		this.sql = sql
	}

	def List columnNames() {}
	abstract def insertSql()
	abstract def mergeSql()
	abstract def batch()
	abstract def params(def line)

	def loadFile(file) {
		processFile(file)
	}

	def mergeFile(file) {
		processFile(file, true)
	}

	private def processFile(file, merge = false) {
		println "${merge ? 'Merging' : 'Loading'} file '$file'"
		def t0 = System.currentTimeMillis()
		List columnNames = columnNames()
		def insertSql = insertSql()
		def mergeSql = mergeSql()
		def batch = batch()
		def csvParams = columnNames ? [columnNames: columnNames, readFirstLine: true] : [:]
		def data = CsvParser.parseCsv(csvParams, new FileReader(file))
		def rows = 0
		for (line in data) {
			sql.execute(params(line), merge ? mergeSql : insertSql)
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

	static def integer(i) {
		i ? i.toInteger() : null
	}

	static def decimal(d) {
		d ? d.toBigDecimal() : null
	}

	static def date(d) {
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
}
