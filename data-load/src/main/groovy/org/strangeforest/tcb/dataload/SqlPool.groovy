package org.strangeforest.tcb.dataload

import java.sql.*
import java.util.concurrent.*
import javax.sql.*

import org.springframework.jdbc.datasource.*

import com.zaxxer.hikari.*
import groovy.sql.*

class SqlPool extends LinkedBlockingDeque<Sql> {

	static final String DB_URL_PROPERTY = 'tcb.db.url'
	static final String USERNAME_PROPERTY = 'tcb.db.username'
	static final String PASSWORD_PROPERTY = 'tcb.db.password'
	static final String DB_CONNECTIONS_PROPERTY = 'tcb.db.connections'

	static final String DB_URL_DEFAULT = 'jdbc:postgresql://localhost:5432/postgres'
	static final String USERNAME_DEFAULT = 'tcb'
	static final String PASSWORD_DEFAULT = 'tcb'
	static final int DB_CONNECTIONS_DEFAULT = 2

	SqlPool(size = null) {
		print 'Allocating DB connections'
		def conns = size ?: connections
		for (int i = 0; i < conns; i++) {
			Sql sql = Sql.newInstance(dbURL + '?prepareThreshold=1', username, password, 'org.postgresql.Driver')
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
			withTx(sql, c)
		}
		finally {
			put(sql)
		}
	}

	static withTx(Sql sql, Closure c) {
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
	}

	static DataSource dataSource() {
		def dbURL = System.getProperty(DB_URL_PROPERTY, DB_URL_DEFAULT)
		def username = System.getProperty(USERNAME_PROPERTY, USERNAME_DEFAULT)
		def password = System.getProperty(PASSWORD_PROPERTY, PASSWORD_DEFAULT)
		new DriverManagerDataSource(dbURL, username, password)
	}

	static DataSource connectionPoolDataSource(size = null) {
		HikariConfig config = new HikariConfig()
		config.jdbcUrl = dbURL
		config.username = username
		config.password = password
		config.maximumPoolSize = size ?: connections
		config.poolName = 'TCB'
		config.dataSourceProperties = [prepareThreshold: 1, reWriteBatchedInserts: true]
		new HikariDataSource(config)
	}

	static String getDbURL() {
		System.getProperty(DB_URL_PROPERTY, DB_URL_DEFAULT)
	}

	static String getUsername() {
		System.getProperty(USERNAME_PROPERTY, USERNAME_DEFAULT)
	}

	static String getPassword() {
		System.getProperty(PASSWORD_PROPERTY, PASSWORD_DEFAULT)
	}

	static int getConnections() {
		Integer.parseInt(System.getProperty(DB_CONNECTIONS_PROPERTY, String.valueOf(DB_CONNECTIONS_DEFAULT)))
	}
}
