package org.strangeforest.tcb.dataload

import org.springframework.jdbc.core.namedparam.*
import org.strangeforest.tcb.stats.model.records.*
import org.strangeforest.tcb.stats.service.*
import org.strangeforest.tcb.util.*

import com.google.common.base.*
import groovy.sql.*

import static org.strangeforest.tcb.dataload.LoadParams.*

class RecordsLoader {

	RecordsService recordsService
	long pause

	RecordsLoader() {
		recordsService = new RecordsService(new NamedParameterJdbcTemplate(SqlPool.dataSource()))
		pause = getLongProperty(RECORD_PAUSE_PROPERTY, RECORD_PAUSE_DEFAULT)
	}

	def loadRecords(ATPTennisLoader atpTennisLoader, Sql sql) {
		println 'Loading records'
		def stopwatch = Stopwatch.createStarted()
		doLoadRecords(Records.getRecordCategories(), 'famous')
		doPause()
		doLoadRecords(Records.getInfamousRecordCategories(), 'infamous')
		doPause()
		atpTennisLoader.refreshMaterializedViews(sql, 'player_goat_points')
		doPause()
		reloadRecordsGOATPointsRecords()
		recordsService.clearActivePlayersRecords()
		println "Records loaded in $stopwatch"
	}

	private doLoadRecords(List<RecordCategory> categories, String name) {
		println "Loading $name records"
		def progress = createTicker()
		def stopwatch = Stopwatch.createStarted()
		for (RecordCategory recordCategory : categories) {
			for (Record record : recordCategory.getRecords()) {
				recordsService.refreshRecord(record.getId(), false)
				progress.tick()
			}
		}
		println "\nLoading $name records finished in $stopwatch"
	}

	private reloadRecordsGOATPointsRecords() {
		print 'Reloading Records GOAT Points records'
		def stopwatch = Stopwatch.createStarted()
		doLoadRecords('GOATPoints', 'AchievementsGOATPoints', 'RecordsGOATPoints')
		recordsService.clearActivePlayersRecords()
		println "\nRecords GOAT Points records reloaded in $stopwatch"
	}

	private doLoadRecords(String... recordIds) {
		def progress = createTicker()
		for (String recordId : recordIds) {
			recordsService.refreshRecord(recordId, false)
			progress.tick()
		}
	}

	private ProgressTicker createTicker() {
		new ProgressTicker('.' as char, 1).withDownstreamTicker(ProgressTicker.newLineTicker().withPostAction({ doPause() }))
	}

	private doPause() {
		System.gc()
		Thread.sleep(pause)
	}
}
