package org.strangeforest.tcb.dataload

import groovy.sql.*

import java.time.*
import java.util.concurrent.*

import static java.lang.Math.*
import static org.strangeforest.tcb.util.DateUtil.*

class EloRatings {

	private BlockingDeque<Sql> sqlPool
	private Map<Player, EloRating> playerRatings = [:]

	private static final String QUERY_MATCHES = //language=SQL
		'SELECT m.winner_id, w.name winner, m.loser_id, l.name loser, e.date, e.level, m.round\n' +
		'FROM match m\n' +
		'INNER JOIN tournament_event e USING (tournament_event_id)\n' +
		'INNER JOIN player_v w ON (w.player_id = m.winner_id)\n' +
		'INNER JOIN player_v l ON (l.player_id = m.loser_id)\n' +
		'ORDER BY e.date, m.match_num'

	private static def comparator = { a, b -> b.value <=> a.value }
	private static def bestComparator = { a, b -> b.value.bestRating <=> a.value.bestRating }

	EloRatings(BlockingDeque<Sql> sqlPool) {
		this.sqlPool = sqlPool
	}

	def compute() {
		def sql = sqlPool.take()
		try {
			sql.eachRow(QUERY_MATCHES, { match -> processMatch(match) })
		}
		finally {
			sqlPool.put(sql)
		}
		playerRatings
	}

	def current() {
		def minDate = toDate(LocalDate.now().minusYears(1))
		playerRatings.findAll {	it.value.matches >= 20 && it.value.date >= minDate	}.sort comparator
	}

	def allTime() {
		playerRatings.findAll {	it.value.matches >= 20 }.sort bestComparator
	}

	def processMatch(match) {
		def winnerId = match.winner_id
		def loserId = match.loser_id

		def winnerRating = getRating(winnerId)
		def loserRating = getRating(loserId)

		def winnerQ = pow(10, winnerRating.rating / 400)
		def loserQ = pow(10, loserRating.rating / 400)
		def loserExpectedScore = loserQ / (winnerQ + loserQ)


		def deltaRating = kFactor(match) * loserExpectedScore
		def date = match.date
		playerRatings.put new Player(id: winnerId, name: match.winner), winnerRating.newRating(deltaRating, date)
		playerRatings.put new Player(id: loserId, name: match.loser), loserRating.newRating(-deltaRating, date)
	}

	static int kFactor(match) {
		switch (match.level) {
			case 'G': return 40
			case 'F': return 30
			case 'M': return 20
			case 'A': return 10
			default: return 10
		}
	}

	private EloRating getRating(playerId) {
		playerRatings.get(new Player(id: playerId)) ?: new EloRating()
	}

	static class Player {

		int id
		String name

		boolean equals(player) {
			this.is(player) || (getClass() == player.class && id == player.id)
		}

		int hashCode() {
			id
		}

		String toString() {
			name
		}
	}

	static class EloRating implements Comparable<EloRating> {

		double rating
		Date date
		int matches
		EloRating bestRating

		EloRating() {
			rating = 1500
			matches = 0
		}

		EloRating newRating(double delta, Date date) {
			def newRating = new EloRating(rating: rating + kFunction(delta), date: date, matches: matches + 1)
			newRating.bestRating = bestRating && bestRating >= newRating ? bestRating : newRating
			newRating
		}

		def double kFunction(double delta) {
			delta * 30 / min(matches + 10, 60)
		}

		String toString() {
			round rating
		}

		int compareTo(EloRating eloRating) {
			rating <=> eloRating.rating
		}
	}
}
