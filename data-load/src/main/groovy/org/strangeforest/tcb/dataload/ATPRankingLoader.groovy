package org.strangeforest.tcb.dataload

import org.jsoup.*

import groovy.sql.*

public class ATPRankingLoader {

	private final Sql sql

	private static final int TIMEOUT = 10 * 1000L

	public ATPRankingLoader(Sql sql) {
		this.sql = sql
	}

	def load(String rankDate, int playerCount) {
		def parsedDate = date rankDate
		def doc = Jsoup.connect(rankingsURL(rankDate, playerCount)).timeout(TIMEOUT).get()
		def paramsBatch = []
		doc.select("tbody tr").each {
			def player = player it.select('td.player-cell').text().replace('-', ' ')
			def rank = integer it.select('td.rank-cell').text()
			def points = integer it.select('td.points-cell').text().replace(',', '')
			paramsBatch.add([rank_date: parsedDate, player_name: player, rank: rank, rank_points: points])
		}
		sql.withBatch('{call load_ranking(:rank_date, :player_name, :rank, :rank_points)}') { ps ->
			paramsBatch.each { params ->
				ps.addBatch(params)
			}
		}
		println "$rankDate: $paramsBatch.size rankings loaded"
	}

	static player(String name) {
		switch (name) {
			case 'Albert Ramos Vinolas': return 'Albert Ramos'
			case 'Diego Schwartzman': return 'Diego Sebastian Schwartzman'
			case 'Duckhee Lee': return 'Duck Hee Lee'
			case 'Frances Tiafoe': return 'Francis Tiafoe'
			case 'Franko Skugor': return 'Franco Skugor'
			case 'Inigo Cervantes': return 'Inigo Cervantes Huegun'
			case 'Sam Groth': return 'Samuel Groth'
			case 'Stan Wawrinka': return 'Stanislas Wawrinka'
			case 'Taylor Fritz': return 'Taylor Harry Fritz'
			case 'Victor Estrella Burgos': return 'Victor Estrella'
			default: return name;
		}
	}

	static rankingsURL(String date, int topN) {
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
