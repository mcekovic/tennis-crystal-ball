package org.strangeforest.tcb.dataload

import groovy.sql.*
import org.springframework.jdbc.datasource.*

import javax.sql.*
import java.sql.*
import java.util.concurrent.*

class SqlPool extends LinkedBlockingDeque<Sql> {

	static final MIN_SIZE = 2

	SqlPool(size = null) {
		print 'Allocating DB connections'
		def dbURL = System.getProperty('tcb.db.url', 'jdbc:postgresql://localhost:5432/postgres?prepareThreshold=0')
		def username = System.getProperty('tcb.db.username', 'tcb')
		def password = System.getProperty('tcb.db.password', 'tcb')
		def connections = size ?: Math.max(MIN_SIZE, Integer.parseInt(System.getProperty('tcb.db.connections', String.valueOf(MIN_SIZE))))

		for (int i = 0; i < connections; i++) {
			Sql sql = Sql.newInstance(dbURL, username, password, 'org.postgresql.Driver')
			sql.connection.autoCommit = false
			sql.cacheStatements = true
			addFirst(sql)
			print '.'
		}
		println()
	}

	def withSql(Closure c) {
		Sql sql = take()
		try {
			def r = c(sql)
			sql.commit()
			r
		}
		catch (BatchUpdateException buEx) {
			sql.rollback()
			for (def nextEx = buEx.getNextException(); nextEx ; nextEx = nextEx.getNextException())
				System.err.println(nextEx)
			throw buEx
		}
		catch (Throwable th) {
			sql.rollback()
			throw th
		}
		finally {
			put(sql)
		}
	}

	static DataSource dataSource() {
		def dbURL = System.getProperty('tcb.db.url', 'jdbc:postgresql://localhost:5432/postgres?prepareThreshold=0')
		def username = System.getProperty('tcb.db.username', 'tcb')
		def password = System.getProperty('tcb.db.password', 'tcb')
		new DriverManagerDataSource(dbURL, username, password)
	}
}
