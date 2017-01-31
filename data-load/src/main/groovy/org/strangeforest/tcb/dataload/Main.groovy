package org.strangeforest.tcb.dataload

def cli = new CliBuilder(usage: 'data-load [commands]', header: 'Commands:')
cli.c('Create database objects')
cli.d('Drop database objects')
cli.p('Load additional player data')
cli.a('Load add-hoc rankings and tournaments')
cli.e('Compute Elo ratings')
cli.r('Refresh computed data')
cli.rc('Refresh Records')
cli.v('Vacuum space')
def options = cli.parse(args)

if (options && (options.c || options.d || options.p || options.a || options.e || options.r || options.rc || options.v)) {
	if (options.c)
		callLoader('createDatabase')
	if (options.d)
		callLoader('dropDatabase')
	if (options.p)
		callLoader('loadAdditionalPlayerData')
	if (options.a) {
		new LoadAdHocRankings().run()
		new LoadAdHocTournaments().run()
	}
	if (options.e)
		new ComputeEloRatings().run()
	if (options.r)
		callLoader('refreshMaterializedViews')
	if (options.rc)
		new RecordsLoader().loadRecords()
	if (options.v)
		callLoader('vacuum')
}
else
	println cli.usage()

def callLoader(methodName) {
	new SqlPool().withSql { sql ->
		new ATPTennisLoader()."$methodName"(sql)
	}
}