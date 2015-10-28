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
	boolean withBatch() { true }
	abstract int batchSize()
	abstract Map params(def line)

	def loadFile(String file) {
		println "Loading file '$file'"
		def t0 = System.currentTimeMillis()
		List columnNames = columnNames()
		def loadSql = loadSql()
		def batchSize = batchSize()
		def csvParams = columnNames ? [columnNames: columnNames, readFirstLine: true] : [:]
		def data = CsvParser.parseCsv(csvParams, new FileReader(file))
		int rows = withBatch() ? loadWithBatch(data, loadSql, batchSize) : load(data, loadSql, batchSize)
		println ''
		def seconds = (System.currentTimeMillis() - t0) / 1000.0
		int rowsPerSecond = rows/seconds
		println "Rows: $rows in $seconds s ($rowsPerSecond row/s)"
		return rows
	}

	def load(Iterator data, String loadSql, int batchSize) {
		def rows = 0
		for (line in data) {
			sql.execute(params(line), loadSql)
			if (++rows % batchSize == 0) {
				sql.commit()
				printProgress(rows, batchSize)
			}
		}
		sql.commit()
		rows
	}

	def loadWithBatch(Iterator data, String loadSql, int batchSize) {
		def rows = 0
		def paramsBatch = []
		for (line in data) {
			paramsBatch.add params(line)
			if (++rows % batchSize == 0) {
				executeBatch(loadSql, paramsBatch)
				printProgress(rows, batchSize)
			}
		}
		if (paramsBatch)
			executeBatch(loadSql, paramsBatch)
		rows
	}

	def executeBatch(String loadSql, Collection paramsBatch) {
		try {
			sql.withBatch(loadSql) { ps ->
				paramsBatch.each { params ->
					ps.addBatch(params)
				}
			}
		}
		catch (BatchUpdateException buEx) {
			def nextEx = buEx.getNextException();
			if (nextEx)
				System.err.println(nextEx);
			throw buEx;
		}
		sql.commit()
		paramsBatch.clear()
	}

	static def printProgress(int rows, int batchSize) {
		print '.'
		if (rows % (batchSize * PROGRESS_LINE_WRAP) == 0)
			println()
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
