package org.strangeforest.tcb.dataload

class LoadParams {

	static final String BASE_DIR_PROPERTY = 'tcb.data.base-dir'
	static final String FULL_LOAD_PROPERTY = 'tcb.data.full-load'
	static final String USE_MATERIALIZED_VIEWS_PROPERTY = 'tcb.data.use-materialized-views'
	static final String MAX_RANK_PROPERTY = 'tcb.data.max-rank'
	static final String VERBOSE_PROPERTY = 'tcb.data.verbose'

	static final boolean FULL_LOAD_DEFAULT = true
	static final boolean USE_MATERIALIZED_VIEWS_DEFAULT = true
	static final boolean VERBOSE_DEFAULT = false
}
