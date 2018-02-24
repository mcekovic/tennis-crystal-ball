package org.strangeforest.tcb.dataload

import static java.lang.Integer.*

class MatchScoreParser {

	static MatchScore parse(String score) {
		if (!score)
			return null
		List sets = score.tokenize(' ')
		short w_sets = 0
		short l_sets = 0
		short w_games = 0
		short l_games = 0
		short w_tbs = 0
		short l_tbs = 0
		List setScores = new ArrayList(sets.size())
		String outcome = null
		for (String set in sets) {
			if (set.startsWith('[') && set.endsWith(']'))
				set = set.substring(1, set.length() - 1)
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
					Short w_tb = w_gms == (l_gms + 1) && l_gms >= 6 ? 1 : null
					Short l_tb = l_gms == (w_gms + 1) && w_gms >= 6 ? 1 : null
					if (w_tb)
						++w_tbs
					if (l_tb)
						++l_tbs
					setScores << new SetScore(
						w_games: w_gms, l_games: l_gms,
						w_tb_pt: tb_pt >= 0 ? (w_win ? max(tb_pt + 2, 7) : tb_pt) : null,
						l_tb_pt: tb_pt >= 0 ? (l_win ? max(tb_pt + 2, 7) : tb_pt) : null,
						w_tbs: w_tb,
						l_tbs: l_tb
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
					case '(W/O)':
					case 'Walkover': outcome = 'W/O'; break
					case 'RET':
					case '(RET)': outcome = setScores.isEmpty() ? 'W/O' : 'RET'; break
					case 'ABD':
					case 'ABN':
					case '(ABN)':
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
		def count = setScores.size() - 1
		for (int i = 0; i < count; i++) {
			def s = setScores[i]
			if (isOldWin(s.w_games, s.l_games))
				w_sets++
			else if (isOldWin(s.l_games, s.w_games))
				l_sets++
		}
		if (!outcome && setScores.size() > w_sets + l_sets)
			outcome = 'RET'
		new MatchScore(outcome: outcome, w_sets: w_sets, l_sets: l_sets, w_games: w_games, l_games: l_games, w_tbs: w_tbs, l_tbs: l_tbs, setScores: setScores)
	}

	private static short parseGames(String s) {
		switch (s) {
			case 'Jun': return 6
			default: s.toInteger()
		}
	}

	private static boolean isWin(int w_games, int l_games) {
		(w_games >= 6 && w_games >= l_games + 2) || (w_games == 7 && l_games == 6) || (w_games == 9 && l_games == 8)
	}

	private static boolean isOldWin(int w_games, int l_games) {
		w_games == 6 && l_games == 5
	}
}
