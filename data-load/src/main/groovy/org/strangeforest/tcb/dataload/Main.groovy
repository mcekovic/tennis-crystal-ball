package org.strangeforest.tcb.dataload

def cli = new CliBuilder(usage: 'data-load [commands]', header: 'Commands:')
cli.c('Create database objects')
cli.d('Drop database objects')
cli.a('Load add-hoc tournaments')
cli.e('Compute Elo ratings')
cli.r('Refresh computed data')
cli.v('Vacuum space')
def options = cli.parse(args)

if (options && (options.c || options.d || options.a || options.e || options.r || options.v)) {
	if (options.c)
		callLoader('createDatabase')
	if (options.d)
		callLoader('dropDatabase')
	if (options.a)
		new LoadAdHocTournaments().run()
	if (options.e)
		new ComputeEloRatings().run()
	if (options.r)
		callLoader('refreshMaterializedViews')
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