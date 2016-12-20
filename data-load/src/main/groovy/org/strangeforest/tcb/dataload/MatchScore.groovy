package org.strangeforest.tcb.dataload

class MatchScore {

	short w_sets, l_sets
	short w_games, l_games
	String outcome
	List setScores

	short[] getW_set_games() {
		setScores.collect({setScore -> setScore.w_games});
	}

	short[] getL_set_games() {
		setScores.collect({setScore -> setScore.l_games});
	}

	Short[] getW_set_tb_pt() {
		setScores.collect({setScore -> setScore.w_tb_pt});
	}

	Short[] getL_set_tb_pt() {
		setScores.collect({setScore -> setScore.l_tb_pt});
	}

	Short getBestOf() {
		if (!outcome) {
			if (w_sets == (short)2)
				3
			else if (w_sets == (short)3)
				5
		}
		null
	}

	boolean equals(Object o) {
		if (!(o instanceof MatchScore)) false
		MatchScore score = (MatchScore)o
		w_sets == score.w_sets && l_sets == score.l_sets && w_games == score.w_games && l_games == score.l_games	&& outcome == score.outcome && setScores == score.setScores
	}

	int hashCode() {
		Objects.hash(w_sets, l_sets, w_games, l_games, outcome, setScores)
	}

	String toString() {
		StringBuilder sb = new StringBuilder("MatchScore{")
		sb.append("w_sets=").append(w_sets)
		sb.append(", l_sets=").append(l_sets)
		sb.append(", w_games=").append(w_games)
		sb.append(", l_games=").append(l_games)
		sb.append(", setScores=").append(setScores)
		if (outcome != null)
			sb.append(", outcome='").append(outcome).append('\'')
		sb.append('}')
		sb.toString()
	}
}
