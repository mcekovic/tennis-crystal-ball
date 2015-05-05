package org.strangeforest.tcb.dataload

import static java.lang.Math.*

class MatchScore {

	int w_sets, l_sets
	List setScores
	String outcome

	static MatchScore parse(String match) {
		List sets = match.trim().tokenize(' ')
		int w_sets = 0, l_sets = 0
		List setScores = new ArrayList(sets.size())
		String outcome = null
		for (String set in sets) {
			int pos = set.indexOf('-')
			if (pos > 0) {
				try {
					int len = set.length()
					int w_gems = set.substring(0, pos).toInteger()
					int pos2 = set.indexOf('(', pos + 2)
					int l_gems = set.substring(pos + 1, pos2 > 0 ? pos2 : len).toInteger()
					Integer tb_pt = null;
					if (pos2 > 0) {
						if (set[len - 1] == ')')
							tb_pt = set.substring(pos2 + 1, len - 1).toInteger()
					}
					boolean w_win = isWin(w_gems, l_gems)
					boolean l_win = isWin(l_gems, w_gems)
					if (w_win) w_sets++
					if (l_win) l_sets++
					setScores.add(new SetScore(
						w_gems: w_gems, l_gems: l_gems,
						w_tb_pt: tb_pt >= 0 ? (w_win ? max(tb_pt + 2, 7) : tb_pt) : null,
						l_tb_pt: tb_pt >= 0 ? (l_win ? max(tb_pt + 2, 7) : tb_pt) : null
					))
				}
				catch (Exception ex) {
					println("Invalid set: $set")
					ex.printStackTrace()
				}
			}
			else {
				switch (set) {
					case 'W/O': outcome = 'W/O'; break
					case 'RET':
					case 'DEF':
					case 'ABD':
					case 'ABN': outcome = setScores ? 'RET' : 'W/O'; break
					default: println("Invalid set outcome: $set")
				}
			}
		}
		new MatchScore(outcome: outcome, w_sets: w_sets, l_sets: l_sets, setScores: setScores)
	}

	static boolean isWin(int w_gems, int l_gems) {
		(w_gems >= 6 && w_gems >= l_gems + 2) || (w_gems == 7 && l_gems == 6)
	}


	@Override boolean equals(Object o) {
		if (!(o instanceof MatchScore)) false
		MatchScore score = (MatchScore)o
		Objects.equals(w_sets, score.w_sets) &&
			Objects.equals(l_sets, score.l_sets) &&
			Objects.equals(setScores, score.setScores) &&
			Objects.equals(outcome, score.outcome)
	}

	@Override int hashCode() {
		Objects.hash(w_sets, l_sets, setScores, outcome)
	}


	@Override public String toString() {
		StringBuilder sb = new StringBuilder("MatchScore{")
		sb.append("w_sets=").append(w_sets)
		sb.append(", l_sets=").append(l_sets)
		sb.append(", setScores=").append(setScores)
		if (outcome != null)
			sb.append(", outcome='").append(outcome).append('\'')
		sb.append('}')
		sb.toString()
	}
}
