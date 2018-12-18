package org.strangeforest.tcb.dataload

import org.strangeforest.tcb.stats.model.elo.*

import static org.strangeforest.tcb.dataload.LoadParams.*

abstract class EloRatingsRunner {

	static computeEloRatings(Boolean fullLoad = null) {
		def threads = getIntProperty(THREADS_PROPERTY, THREADS_DEFAULT)
		if (fullLoad == null)
			fullLoad = getBooleanProperty(FULL_LOAD_PROPERTY, FULL_LOAD_DEFAULT)
		def eloRatings = new EloRatingsManager(SqlPool.connectionPoolDataSource(threads + 1))
		eloRatings.compute(true, fullLoad, null, threads)
		eloRatings
	}
}
