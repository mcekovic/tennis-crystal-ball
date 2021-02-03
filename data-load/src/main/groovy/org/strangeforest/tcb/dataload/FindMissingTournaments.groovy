package org.strangeforest.tcb.dataload

import java.time.LocalDate

loadTournaments(new SqlPool())

static loadTournaments(SqlPool sqlPool) {
	sqlPool.withSql {sql ->
		for (season in 1968..LocalDate.now().year)
			LoadNewTournaments.listMissingTournaments(sqlPool, season)
	}
}