package org.strangeforest.tcb.dataload

import groovy.transform.*

@EqualsAndHashCode
class MatchScore {

	short w_sets, l_sets
	short w_games, l_games
	short w_tbs, l_tbs
	String outcome
	List setScores

	short[] getW_set_games() {
		setScores.collect({setScore -> setScore.w_games})
	}

	short[] getL_set_games() {
		setScores.collect({setScore -> setScore.l_games})
	}

	Short[] getW_set_tb_pt() {
		setScores.collect({setScore -> setScore.w_tb_pt})
	}

	Short[] getL_set_tb_pt() {
		setScores.collect({setScore -> setScore.l_tb_pt})
	}

	Short getBestOf() {
		if (!outcome) {
			if (w_sets == (short)2)
				3
			else if (w_sets == (short)3)
				5
		}
		else {
			def sets = w_sets + l_sets
			if (sets > 3 || (sets == 3 && setScores.size() > 3))
				5
		}
		null
	}

	String toString() {
		StringBuilder sb = new StringBuilder(48)
		setScores.eachWithIndex { set, index ->
			if (index > 0)
				sb.append(' ')
			sb.append(set)
		}
		if (outcome) {
			if (sb.length() > 0)
				sb.append(' ')
			sb.append(outcome)
		}
		sb.toString()
	}
}
