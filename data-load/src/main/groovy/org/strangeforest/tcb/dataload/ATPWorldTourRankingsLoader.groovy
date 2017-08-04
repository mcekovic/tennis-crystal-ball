package org.strangeforest.tcb.dataload

import com.google.common.base.*
import groovy.sql.*

import static org.strangeforest.tcb.dataload.LoaderUtil.*

class ATPWorldTourRankingsLoader {

	private final Sql sql

	ATPWorldTourRankingsLoader(Sql sql) {
		this.sql = sql
	}

	def load(String rankDate, int playerCount) {
		def parsedDate = date rankDate
		def url = rankingsUrl(rankDate, playerCount)
		println "Fetching rankings URL '$url'"
		def stopwatch = Stopwatch.createStarted()
		def doc = retriedGetDoc(url)
		def paramsBatch = []
		doc.select("tbody tr").each {
			def player = player it.select('td.player-cell').text()
			def rank = rank it.select('td.rank-cell').text()
			def points = integer it.select('td.points-cell').text().replace(',', '')
			paramsBatch << [rank_date: parsedDate, player_name: player, rank: rank, rank_points: points]
		}
		sql.withBatch('{call load_ranking(:rank_date, :player_name, :rank, :rank_points)}') { ps ->
			paramsBatch.each { params ->
				ps.addBatch(params)
			}
		}
		sql.commit()
		println "$rankDate: $paramsBatch.size rankings loaded in $stopwatch"
	}

	static player(String name) {
		name.replace('-', ' ').replace('.', ' ').replace('\'', '').replace('(', '').replace(')', '').replace('  ', ' ').trim()
	}

	static rank(String rank) {
		integer(rank.endsWith('T') ? rank.substring(0, rank.length() - 1) : rank)
	}

	static rankingsUrl(String date, int topN) {
		"http://www.atpworldtour.com/en/rankings/singles?rankDate=$date&&rankRange=1-$topN"
	}


	// Data conversion

	static Integer integer(i) {
		i ? i.toInteger() : null
	}

	static java.sql.Date date(d) {
		d ? new java.sql.Date(Date.parse('yyyy-MM-dd', d).time) : null
	}
}
