package org.strangeforest.tcb.dataload

import java.sql.*
import java.util.Date
import java.util.concurrent.*

import org.strangeforest.tcb.util.*

import com.google.common.base.*
import groovy.sql.*

abstract class BaseXMLLoader {

	protected final Sql sql

	private static def CLASSPATH_PREFIX = 'classpath:'
	private static def PROGRESS_LINE_WRAP = 100

	BaseXMLLoader(Sql sql) {
		this.sql = sql
	}

	abstract int batch()
	abstract boolean loadItem(item)

	def loadFile(String file) {
		println "Loading file '$file'"
		def stopwatch = Stopwatch.createStarted()
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
		println()
		stopwatch.stop()
		def seconds = stopwatch.elapsed(TimeUnit.SECONDS)
		int rowsPerSecond = seconds ? rows / seconds : 0
		println "Rows: $rows in $stopwatch ($rowsPerSecond row/s)"
		return rows
	}

	private getReader(String file) {
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
		s = s?.toString()
		s ?: d
	}

	static Integer integer(i) {
		i = i?.toString()
		i ? i.toInteger() : null
	}

	static Short smallint(i) {
		i = i?.toString()
		i ? i.toShort() : null
	}

	static Boolean bool(b, d = null) {
		b = b?.toString()
		b ? b.toBoolean() : d
	}

	static Float real(f) {
		f = f?.toFloat()
		f != null ? f.toFloat() : null
	}

	static java.sql.Date date(d) {
		d = d?.toString()
		d ? new java.sql.Date(Date.parse('yyyy-MM-dd', d).time) : null
	}

	static Array shortArray(Connection conn, a) {
		conn.createArrayOf('smallint', (Short[])a)
	}

	static String country(c) {
		c = c?.toString()
		c && Country.code(c) ? c : null
	}
}
