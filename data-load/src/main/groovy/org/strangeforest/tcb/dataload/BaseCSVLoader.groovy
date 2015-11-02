package org.strangeforest.tcb.dataload

import org.strangeforest.tcb.util.CountryUtil

import java.sql.*
import java.util.Date

import com.xlson.groovycsv.*
import groovy.sql.*

import java.util.concurrent.*
import java.util.concurrent.atomic.*

abstract class BaseCSVLoader {

	private BlockingDeque<Sql> sqlPool

	private static def PROGRESS_LINE_WRAP = 100

	BaseCSVLoader(BlockingDeque<Sql> sqlPool) {
		this.sqlPool = sqlPool
	}

	List columnNames() { null }
	int threadCount() { Integer.MAX_VALUE }
	abstract String loadSql()
	abstract int batchSize()
	abstract Map params(line, sql)

	def loadFile(String file) {
		println "Loading file '$file'"
		def t0 = System.currentTimeMillis()
		List columnNames = columnNames()
		def csvParams = columnNames ? [columnNames: columnNames, readFirstLine: true] : [:]
		def data = CsvParser.parseCsv(csvParams, new FileReader(file))
		int rows = load(data)
		println ''
		def seconds = (System.currentTimeMillis() - t0) / 1000.0
		int rowsPerSecond = rows/seconds
		println "Rows: $rows in $seconds s ($rowsPerSecond row/s)"
		return rows
	}

	def load(Iterator data) {
		def loadSql = loadSql()
		def batchSize = batchSize()
		def rows = 0
		def batches = new AtomicInteger()
		def paramsBatch = []
		def sql = sqlPool.removeFirst()
		try {
			def executor = Executors.newFixedThreadPool(Math.min(sqlPool.size(), threadCount()))
			for (line in data) {
				paramsBatch.add params(line, sql)
				if (++rows % batchSize == 0) {
					execute(executor, loadSql, paramsBatch, batches)
					paramsBatch = []
				}
			}
			if (paramsBatch)
				execute(executor, loadSql, paramsBatch, batches)
			executor.shutdown()
			executor.awaitTermination(1L, TimeUnit.DAYS)
		}
		finally {
			sqlPool.addFirst(sql)
		}
		rows
	}

	def execute(ExecutorService executor, String loadSql, Iterable<Map> paramsBatch, AtomicInteger batches) {
		def lineWrap = PROGRESS_LINE_WRAP
		def sqlPool = sqlPool
		executor.execute {
			def sql = sqlPool.removeFirst()
			try {
				executeWithBatch(sql, loadSql, paramsBatch)
				sql.commit()
			}
			finally {
				sqlPool.addFirst(sql)
			}
			print '.'
			if (batches.incrementAndGet() % lineWrap == 0)
				println()
		}
	}

	static def executeWithBatch(Sql sql, String loadSql, Iterable<Map> paramsBatch) {
		try {
			sql.withBatch(loadSql) { ps ->
				paramsBatch.each { params ->
					ps.addBatch(params)
				}
			}
		}
		catch (BatchUpdateException buEx) {
			for (def nextEx = buEx.getNextException(); nextEx ; nextEx = nextEx.getNextException())
				System.err.println(nextEx);
			throw buEx;
		}
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

	static Array shortArray(sql, a) {
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
