package org.strangeforest.tcb.model.elo

import java.lang.IllegalArgumentException

class EloRatings {

	companion object {

		fun kFactor(level: String, round: String, bestOf: Int, outcome: String?): Double {
			return 32.0 * when (level) {
				"G" -> 1.00
				"F" -> 0.90
				"L" -> 0.85
				"M" -> 0.85
				"O" -> 0.80
				"A" -> 0.75
				else -> 0.70
			} * when (round) {
				"F" -> 1.00
				"BR" -> 0.95
				"SF" -> 0.90
				"QF" -> 0.85
				"R16" -> 0.80
				"R32" -> 0.80
				"R64" -> 0.75
				"R128" -> 0.75
				"RR" -> 0.85
				else -> throw IllegalArgumentException()
			} * when (bestOf) {
				5 -> 1.00
				3 -> 0.90
				else -> throw IllegalArgumentException()
			} * when (outcome) {
				null -> 1.00
				"RET" -> 1.00
				"W/O" -> 0.50
				else -> throw IllegalArgumentException()
			}
		}
	}
}