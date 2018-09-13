package org.strangeforest.tcb.dataload

import java.sql.*
import java.text.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

import org.strangeforest.tcb.util.*

import com.google.common.base.*
import com.xlson.groovycsv.*

import static org.strangeforest.tcb.dataload.LoaderUtil.*

abstract class BaseCSVLoader {

	protected final SqlPool sqlPool

	private static final int PROGRESS_LINE_WRAP = 100

	private static final String DEADLOCK_DETECTED = "40P01"

	BaseCSVLoader(SqlPool sqlPool) {
		this.sqlPool = sqlPool
	}

	List columnNames() { null }
	int threadCount() { Integer.MAX_VALUE }
	abstract String loadSql()
	abstract int batchSize()
	abstract Map params(record, Connection conn)

	def loadFile(String file) {
		println "Loading file '$file'"
		def stopwatch = Stopwatch.createStarted()
		List columnNames = columnNames()
		def csvParams = columnNames ? [columnNames: columnNames, readFirstLine: true] : [:]
		def data = CsvParser.parseCsv(csvParams, new FileReader(file))
		int rows = load(data)
		printLoadInfo(stopwatch, rows)
		return rows
	}

	def load(Iterable data) {
		def stopwatch = Stopwatch.createStarted()
		int rows = load(data.iterator())
		printLoadInfo(stopwatch, rows)
		return rows
	}

	static printLoadInfo(Stopwatch stopwatch, int rows) {
		println()
		stopwatch.stop()
		def seconds = stopwatch.elapsed(TimeUnit.SECONDS)
		int rowsPerSecond = seconds ? rows / seconds : 0
		println "Rows: $rows in $stopwatch ($rowsPerSecond row/s)"
	}

	def load(Iterator data) {
		def loadSql = loadSql()
		def batchSize = batchSize()
		def rows = 0
		def batches = new AtomicInteger()
		def paramsBatch = []
		sqlPool.withSql { sql ->
			def executor = Executors.newFixedThreadPool(Math.min(sqlPool.size(), threadCount()))
			def paramsConn = sql.connection
			for (record in data) {
				if (record.values.size() <= 1)
					continue
				def params = params(record, paramsConn)
				if (params) {
					paramsBatch << params
					if (++rows % batchSize == 0) {
						execute(executor, loadSql, paramsBatch, batches)
						paramsBatch = []
					}
				}
			}
			if (paramsBatch)
				execute(executor, loadSql, paramsBatch, batches)
			executor.shutdown()
			executor.awaitTermination(1L, TimeUnit.DAYS)
		}
		rows
	}

	def execute(ExecutorService executor, String loadSql, Collection<Map> paramsBatch, AtomicInteger batches) {
		def lineWrap = PROGRESS_LINE_WRAP
		executor.execute {
			executeWithBatch(loadSql, paramsBatch)
			if (batches.incrementAndGet() % lineWrap == 0)
				println '.'
			else
				print '.'
		}
	}

	def executeWithBatch(String loadSql, Collection<Map> paramsBatch) {
		sqlPool.withSql { sql ->
			try {
				sql.withBatch(loadSql) { ps ->
					paramsBatch.each { params ->
						ps.addBatch(params)
					}
				}
			}
			catch (BatchUpdateException buEx) {
				switch (buEx.getSQLState()) {
					case DEADLOCK_DETECTED:
						print '*'
						for (def paramsSubBatch : tile(paramsBatch))
							executeWithBatch(loadSql, paramsSubBatch)
						break
					default:
						throw buEx
				}
			}
		}
	}

	// Data conversion

	static String string(s, d = null) {
		s ?: d
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

	static BigDecimal safeDecimal(d) {
		try {
			d ? d.toBigDecimal() : null
		}
		catch (NumberFormatException ex) {
			null
		}
	}

	static Float real(f) {
		f ? f.toFloat() : null
	}

	static Date date(d) {
		if (d) {
			switch (d.length()) {
				case 4: d += '0701'; break
				case 6: d += '15'; break
			}
			new Date(new SimpleDateFormat('yyyyMMdd').parse(d).time)
		}
		else
			null
	}

	static Array shortArray(conn, a) {
		conn.createArrayOf('smallint', a)
	}

	static String country(c, d = null) {
		c && Country.code(c) ? c : d
	}

	static String hand(c) {
		switch (c) {
			case 'R': return 'R'
			case 'L': return 'L'
			default: return null
		}
	}

	static Object safeProperty(obj,  String propName) {
		obj.toMap()[propName]
	}
}
