package org.strangeforest.tcb.dataload

def sqlPool = new SqlPool()
sqlPool.withSql { sql ->
	def loader = new WikipediaPlayerDataLoader(sql)
	loader.updatePlayerData()
}
