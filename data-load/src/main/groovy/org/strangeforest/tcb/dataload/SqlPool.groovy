package org.strangeforest.tcb.dataload

import java.util.concurrent.*

import groovy.sql.*

class SqlPool {

	static final CONNECTIONS = 2

	static BlockingDeque<Sql> create() {
		print 'Allocating DB connections'
		def dbURL = System.getProperty('tcb.db.url', 'jdbc:postgresql://localhost:5432/postgres?prepareThreshold=0')
		def username = System.getProperty('tcb.db.username', 'tcb')
		def password = System.getProperty('tcb.db.password', 'tcb')
		def connections = Math.max(CONNECTIONS, Integer.parseInt(System.getProperty('tcb.db.connections', String.valueOf(CONNECTIONS))))

		def sqlPool = new LinkedBlockingDeque<Sql>()
		for (int i = 0; i < connections; i++) {
			Sql sql = Sql.newInstance(dbURL, username, password, 'org.postgresql.Driver')
			sql.connection.autoCommit = false
			sql.cacheStatements = true
			sqlPool.addFirst(sql)
			print '.'
		}
		println ''
		sqlPool
	}
}
