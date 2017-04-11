package org.strangeforest.tcb.dataload

def cli = new CliBuilder(usage: 'data-load [commands]', header: 'Commands:')
cli.c(args: 1, argName: 'DB connections', 'Number of database connections to allocate')
cli.cd('Create database objects')
cli.dd('Drop database objects')
cli.lp('Load additional player data')
cli.la('Load ad-hoc rankings and tournaments')
cli.ip('Load in-progress tournaments')
cli.ce('Compute Elo ratings')
cli.rc('Refresh computed data')
cli.rr('Refresh Records')
cli.vc('Vacuum space')
cli.help('Print this message')
def options = cli.parse(args)

if (options && (options.cd || options.dd || options.lp || options.la || options.ip || options.ce || options.rc || options.rr || options.vc)) {
	if (options.c) {
		def dbConns = String.valueOf(options.getProperty('c')).trim()
		System.setProperty(SqlPool.DB_CONNECTIONS_PROPERTY, dbConns)
	}
	if (options.cd)
		callLoader('createDatabase')
	if (options.dd)
		callLoader('dropDatabase')
	if (options.lp)
		callLoader('loadAdditionalPlayerData')
	if (options.la) {
		new LoadAdHocRankings().run()
		new LoadAdHocTournaments().run()
	}
	if (options.ip)
		new LoadInProgressTournaments().run()
	if (options.ce)
		new ComputeEloRatings().run()
	if (options.rc)
		callLoader('refreshMaterializedViews')
	if (options.rr)
		new RecordsLoader().loadRecords()
	if (options.vc)
		callLoader('vacuum')
}
else
	cli.usage()

static def callLoader(methodName) {
	new SqlPool().withSql { sql ->
		new ATPTennisLoader()."$methodName"(sql)
	}
}