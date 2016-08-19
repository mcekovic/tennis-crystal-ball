package org.strangeforest.tcb.dataload

import static java.lang.Math.*

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

	static MatchScore parse(String match) {
		if (!match)
			return null
		List sets = match.tokenize(' ')
		short w_sets = 0, l_sets = 0
		short w_games = 0, l_games = 0
		List setScores = new ArrayList(sets.size())
		String outcome = null
		for (String set in sets) {
			int pos = set.indexOf('-')
			if (pos > 0) {
				try {
					int len = set.length()
					short w_gms = parseGames(set.substring(0, pos))
					int pos2 = set.indexOf('(', pos + 2)
					short l_gms = parseGames(set.substring(pos + 1, pos2 > 0 ? pos2 : len))
					Short tb_pt = null
					if (pos2 > 0) {
						if (set[len - 1] == ')')
							tb_pt = set.substring(pos2 + 1, len - 1).toInteger()
					}
					boolean w_win = isWin(w_gms, l_gms)
					boolean l_win = isWin(l_gms, w_gms)
					if (w_win) w_sets++
					if (l_win) l_sets++
					w_games += w_gms
					l_games += l_gms
					if (tb_pt >= 0) {
						if (w_win) w_games--
						if (l_win) l_games--
					}
					setScores << new SetScore(
						w_games: w_gms, l_games: l_gms,
						w_tb_pt: tb_pt >= 0 ? (w_win ? max(tb_pt + 2, 7) : tb_pt) : null,
						l_tb_pt: tb_pt >= 0 ? (l_win ? max(tb_pt + 2, 7) : tb_pt) : null
					)
				}
				catch (Exception ex) {
					println("Invalid set: $set")
					ex.printStackTrace()
				}
			}
			else {
				switch (set) {
					case 'W/O':
					case '(W/O)':	outcome = 'W/O'; break
					case 'RET':
					case '(RET)': outcome = setScores.isEmpty() ? 'W/O' : 'RET'; break
					case 'ABD':
					case 'ABN':
					case 'abandoned':
					case 'unfinished':
					case 'Unfinished': outcome = 'ABD'; break
					case 'Default':
					case 'DEF': outcome = 'DEF'; break
					case 'NA': return null
					case 'In':
					case 'Progress':
					case 'Played':
					case 'and': break
					default: println("Invalid set outcome: $set")
				}
			}
		}
		new MatchScore(outcome: outcome, w_sets: w_sets, l_sets: l_sets, w_games: w_games, l_games: l_games, setScores: setScores)
	}

	private static short parseGames(String s) {
		switch (s) {
			case 'Jun': return 6
			default: s.toInteger()
		}
	}

	private static boolean isWin(int w_games, int l_games) {
		(w_games >= 6 && w_games >= l_games + 2) || (w_games == 7 && l_games == 6)
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
