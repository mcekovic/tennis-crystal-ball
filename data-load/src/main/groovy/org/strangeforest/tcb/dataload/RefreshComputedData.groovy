package org.strangeforest.tcb.dataload

new SqlPool().withSql { sql ->
	new ATPTennisLoader().refreshComputedData(sql)
}
