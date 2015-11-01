package org.strangeforest.tcb.dataload

import groovy.sql.*

import java.util.concurrent.*

class SqlPool {

	def static BlockingDeque<Sql> create() {
		print 'Allocating DB connections'
		def dbURL = System.getProperty('tcb.db.url', 'jdbc:postgresql://localhost:5432/postgres')
		def username = System.getProperty('tcb.db.username', 'tcb')
		def password = System.getProperty('tcb.db.password', 'tcb')
		def connections = Integer.parseInt(System.getProperty('tcb.db.connections', '2'))

		def sqls = new LinkedBlockingDeque<Sql>()
		for (int i = 0; i < connections; i++) {
			Sql sql = Sql.newInstance(dbURL, username, password, 'org.postgresql.Driver')
			sql.connection.autoCommit = false
			sqls.addFirst(sql)
			print '.'
		}
		println ''
		sqls
	}
}
