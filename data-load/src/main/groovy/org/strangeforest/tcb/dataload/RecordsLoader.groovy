package org.strangeforest.tcb.dataload

import com.google.common.base.*
import org.springframework.jdbc.core.namedparam.*
import org.strangeforest.tcb.stats.model.records.*
import org.strangeforest.tcb.stats.service.*

import java.util.concurrent.atomic.*

class RecordsLoader {

	RecordsService recordsService

	static final int PROGRESS_LINE_WRAP = 100

	RecordsLoader() {
		recordsService = new RecordsService(new NamedParameterJdbcTemplate(SqlPool.dataSource()))
	}

	def loadRecords() {
		println 'Loading records'
		def stopwatch = Stopwatch.createStarted()
		doLoadRecords(Records.getRecordCategories(), 'famous')
		doLoadRecords(Records.getInfamousRecordCategories(), 'infamous')
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

	private static progressTick(progress) {
		print '.'
		if (progress.incrementAndGet() % PROGRESS_LINE_WRAP == 0)
			println()
	}
}
