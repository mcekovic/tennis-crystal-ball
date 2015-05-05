package org.strangeforest.tcb.dataload;

import java.util.*;

class SetScore {

	int w_gems, l_gems;
	Integer w_tb_pt, l_tb_pt;

	@Override boolean equals(Object o) {
		if (!(o instanceof SetScore)) false
		SetScore score = (SetScore)o
		Objects.equals(w_gems, score.w_gems) &&
			Objects.equals(l_gems, score.l_gems) &&
			Objects.equals(w_tb_pt, score.w_tb_pt) &&
			Objects.equals(l_tb_pt, score.l_tb_pt)
	}

	@Override int hashCode() {
		Objects.hash(w_gems, l_gems, w_tb_pt, l_tb_pt)
	}


	@Override String toString() {
		StringBuilder sb = new StringBuilder("SetScore{")
		sb.append("w_gems=").append(w_gems)
		sb.append(", l_gems=").append(l_gems)
		if (w_tb_pt != null)
			sb.append(", w_tb_pt=").append(w_tb_pt)
		if (l_tb_pt != null)
			sb.append(", l_tb_pt=").append(l_tb_pt)
		sb.append('}')
		sb.toString()
	}
}
