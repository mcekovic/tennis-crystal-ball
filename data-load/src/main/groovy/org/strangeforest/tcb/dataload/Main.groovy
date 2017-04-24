package org.strangeforest.tcb.dataload

import static org.strangeforest.tcb.dataload.LoadParams.*

def cli = new CliBuilder(usage: 'data-load [commands]', header: 'Commands:')
cli.c(args: 1, argName: 'DB connections', 'Number of database connections to allocate [default 2]')
cli.f('Full load [default]')
cli.d('Delta load')
cli.m('Use materialized views for computed data [default]')
cli.t('Use tables for computed data')
cli.cd('Create database objects')
cli.dd('Drop database objects')
cli.lt('Load atp_tennis data')
cli.lp('Load additional player data')
cli.la('Load ad-hoc rankings and tournaments')
cli.nt('Load new completed tournaments')
cli.nr('Load new rankings')
cli.ip('Load in-progress tournaments')
cli.el('Compute Elo ratings')
cli.rc('Refresh computed data')
cli.rr('Refresh Records')
cli.vc('Vacuum space')
cli.cc('Clear caches')
cli.help('Print this message')
def options = cli.parse(args)

if (options && (options.cd || options.dd || options.lt || options.lp || options.la || options.nt || options.nr || options.ip || options.el || options.rc || options.rr || options.vc || options.cc)) {
	setProperties(options)
	
	if (options.cd)
		callLoader('createDatabase')
	if (options.dd)
		callLoader('dropDatabase')
	if (options.lt)
		new LoadATPTennis().run()
	if (options.lp)
		callLoader('loadAdditionalPlayerData')
	if (options.la) {
		new LoadAdHocRankings().run()
		new LoadAdHocTournaments().run()
	}
	if (options.nt)
		new LoadNewTournaments().run()
	if (options.nr)
		new LoadNewRankings().run()
	if (options.ip)
		new LoadInProgressTournaments().run()
	if (options.el)
		new ComputeEloRatings().run()
	if (options.rc) {
		callLoader('correctData')
		callLoader('refreshMaterializedViews')
	}
	if (options.rr)
		new RecordsLoader().loadRecords()
	if (options.vc)
		callLoader('vacuum')
	if (options.cc)
		new ClearCaches().run()
}
else
	cli.usage()

def setProperties(def options) {
	if (options.c) {
		def dbConns = String.valueOf(options.getProperty('c')).trim()
		System.setProperty(SqlPool.DB_CONNECTIONS_PROPERTY, dbConns)
	}
	
	if (options.f)
		System.setProperty(FULL_LOAD_PROPERTY, 'true')
	else if (options.d)
		System.setProperty(FULL_LOAD_PROPERTY, 'false')

	if (options.m)
		System.setProperty(USE_MATERIALIZED_VIEWS_PROPERTY, 'true')
	else if (options.t)
		System.setProperty(USE_MATERIALIZED_VIEWS_PROPERTY, 'false')
}

static def callLoader(methodName) {
	new SqlPool().withSql { sql ->
		new ATPTennisLoader()."$methodName"(sql)
	}
}