package org.strangeforest.tcb.dataload

def cli = new CliBuilder(usage: 'data-load [commands]', header: 'Commands:')
cli.c('Create database objects')
cli.d('Drop database objects')
cli.r('Refresh computed data')
cli.e('Compute Elo ratings')
cli.a('Load add-hoc tournaments')
def options = cli.parse(args)

if (options && (options.c || options.d || options.r || options.e || options.a)) {
	if (options.c)
		callLoader('createDatabase')
	if (options.d)
		callLoader('dropDatabase')
	if (options.r)
		callLoader('refreshMaterializedViews')
	if (options.e)
		new ComputeEloRatings().run()
	if (options.a)
		new LoadAdHocTournaments().run()
}
else
	println cli.usage()

def callLoader(methodName) {
	new SqlPool().withSql { sql ->
		new ATPTennisLoader()."$methodName"(sql)
	}
}