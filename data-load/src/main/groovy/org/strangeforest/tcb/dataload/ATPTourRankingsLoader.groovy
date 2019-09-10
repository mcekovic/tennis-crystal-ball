package org.strangeforest.tcb.dataload

import java.sql.*
import java.text.*

import com.google.common.base.*
import groovy.sql.*

import static org.strangeforest.tcb.dataload.LoaderUtil.*
import static org.strangeforest.tcb.dataload.SqlPool.*

class ATPTourRankingsLoader {

	private final Sql sql

	ATPTourRankingsLoader(Sql sql) {
		this.sql = sql
	}

	def load(String rankDate, int playerCount, List skipPlayers = []) {
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
			if (!skipPlayers.contains(player))
				paramsBatch << [rank_date: parsedDate, player_name: player, rank: rank, rank_points: points]
		}
		if (paramsBatch) {
			withTx sql, { Sql s ->
				s.withBatch('{call load_ranking(:rank_date, :player_name, :rank, :rank_points)}') { ps ->
					paramsBatch.each { params ->
						ps.addBatch(params)
					}
				}
			}
			println "$rankDate: $paramsBatch.size rankings loaded in $stopwatch"
		}
		else
			println "No rankings found for date $rankDate"
	}

	static player(String name) {
		name.replace('-', ' ').replace('.', ' ').replace('\'', '').replace('(', '').replace(')', '').replace('  ', ' ').trim()
	}

	static rank(String rank) {
		integer(rank.endsWith('T') ? rank.substring(0, rank.length() - 1) : rank)
	}

	static rankingsUrl(String date, int topN) {
		"https://www.atptour.com/en/rankings/singles?rankDate=$date&rankRange=1-$topN"
	}

	def rankDates() {
		def rankDates = []
		def doc = retriedGetDoc('https://www.atptour.com/en/rankings/singles')
		doc.select('div.dropdown-holder-wrapper:nth-child(1) > div.dropdown-holder > ul.dropdown > li').each {
			def rankDate = it.attr('data-value')
			if (rankDate)
				rankDates << rankDate
		}
		rankDates
	}

	def playerCount(String rankDate) {
		def parsedDate = date rankDate
		def url = rankingsUrl(rankDate, 5000)
		def doc = retriedGetDoc(url)
		doc.select("tbody tr").size()
	}

	def delete(String rankDate) {
		def count = sql.executeUpdate(['rankDate': rankDate], 'DELETE FROM player_ranking WHERE rank_date = :rankDate::DATE')
		println "Deleted $count rankings for date $rankDate"
	}


	// Data conversion

	static Integer integer(i) {
		i ? i.toInteger() : null
	}

	static Date date(d) {
		d ? new Date(new SimpleDateFormat('yyyy-MM-dd').parse(d).time) : null
	}
}
