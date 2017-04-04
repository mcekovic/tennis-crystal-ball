package org.strangeforest.tcb.dataload

class TournamentMatches {

	Map entries

	TournamentMatches(List matches) {
		def entryRound = matches[0].round
		entries = [:]
		def matchMap = [:]
		matches.each { match ->
			matchMap[match.match_num] = match
			if (match.prev_match_num1) {
				def prevMatch1 = matchMap[match.prev_match_num1]
				match.prev_match1 = prevMatch1
				prevMatch1.next_match = match
			}
			if (match.prev_match_num2) {
				def prevMatch2 = matchMap[match.prev_match_num2]
				match.prev_match2 = prevMatch2
				prevMatch2.next_match = match
			}
			if (match.round == entryRound) {
				if (match.player1_id)
					entries[match.player1_id] = match
				if (match.player2_id)
					entries[match.player2_id] = match
			}
		}
	}

	def playerIds() {
		entries.keySet()
	}

	def entryMatch(int playerId, String baseResult) {
		findEntryMatch(playerId, baseResult, entries[playerId])
	}

	def findEntryMatch(int playerId, String baseResult, match) {
		if (match.round == baseResult)
			return match
		if (hasWon(match, playerId)) {
			def nextMatch = match.next_match
			if (nextMatch)
				return findEntryMatch(playerId, baseResult, nextMatch)
		}
		null
	}

	static hasWon(match, int playerId) {
		(match.winner == 1 && match.player1_id == playerId) || (match.winner == 2 && match.player2_id == playerId)
	}

}
