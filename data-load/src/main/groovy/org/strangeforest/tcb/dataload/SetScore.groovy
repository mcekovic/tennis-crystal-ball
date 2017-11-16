package org.strangeforest.tcb.dataload

import groovy.transform.*

@EqualsAndHashCode
class SetScore {

	short w_games, l_games
	Short w_tb_pt, l_tb_pt
	Short w_tbs, l_tbs

	String toString() {
		StringBuilder sb = new StringBuilder(8)
		sb.append(w_games).append('-').append(l_games)
		if (l_tb_pt != null || w_tb_pt != null)
			sb.append('(').append(w_games >= l_games ? l_tb_pt : w_tb_pt).append(')')
		sb.toString()
	}
}
