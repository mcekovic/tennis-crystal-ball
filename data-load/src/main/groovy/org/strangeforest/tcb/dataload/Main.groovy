package org.strangeforest.tcb.dataload

def cli = new CliBuilder(usage: 'data-load [commands]', header: 'Commands:')
cli.e('Compute Elo ratings')
cli.r('Refresh computed data')
cli.a('Load add-hoc tournaments')
def options = cli.parse(args)

if (options && (options.e || options.r || options.a)) {
	if (options.e)
		new ComputeEloRatings().run()
	if (options.r)
		new RefreshComputedData().run()
	if (options.a)
		new LoadAdHocTournaments().run()
}
else
	println cli.usage()