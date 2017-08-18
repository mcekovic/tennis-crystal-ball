package org.strangeforest.tcb.dataload

import com.google.common.base.*
import groovy.sql.*
import org.springframework.jdbc.core.namedparam.*
import org.strangeforest.tcb.stats.model.records.*
import org.strangeforest.tcb.stats.service.*

import java.util.concurrent.atomic.*

import static org.strangeforest.tcb.dataload.LoadParams.*

class RecordsLoader {

	RecordsService recordsService
	long pause

	static final int PROGRESS_LINE_WRAP = 100

	RecordsLoader() {
		recordsService = new RecordsService(new NamedParameterJdbcTemplate(SqlPool.dataSource()))
		pause = getLongProperty(RECORD_PAUSE_PROPERTY, RECORD_PAUSE_DEFAULT)
	}

	def loadRecords(ATPTennisLoader atpTennisLoader, Sql sql) {
		println 'Loading records'
		def stopwatch = Stopwatch.createStarted()
		doLoadRecords(Records.getRecordCategories(), 'famous')
		doLoadRecords(Records.getInfamousRecordCategories(), 'infamous')
		atpTennisLoader.refreshMaterializedViews(sql, 'player_goat_points')
		reloadRecordsGOATPointsRecords()
		recordsService.clearActivePlayersRecords()
		println "Records loaded in $stopwatch"
	}

	private doLoadRecords(List<RecordCategory> categories, String name) {
		println "Loading $name records"
		def progress = new AtomicInteger()
		def stopwatch = Stopwatch.createStarted()
		for (RecordCategory recordCategory : categories) {
			for (Record record : recordCategory.getRecords()) {
				recordsService.refreshRecord(record.getId(), false)
				progressTick(progress)
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
		def progress = new AtomicInteger()
		for (String recordId : recordIds) {
			recordsService.refreshRecord(recordId, false)
			progressTick(progress)
		}
	}

	private progressTick(progress) {
		print '.'
		Thread.sleep(pause)
		if (progress.incrementAndGet() % PROGRESS_LINE_WRAP == 0) {
			println()
			System.gc()
			Thread.sleep(pause * 10)
		}
	}
}
