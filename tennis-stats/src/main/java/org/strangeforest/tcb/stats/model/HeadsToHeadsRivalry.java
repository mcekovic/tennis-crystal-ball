package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.model.core.*;

public class HeadsToHeadsRivalry extends Rivalry {

	public HeadsToHeadsRivalry(RivalryPlayer player1, RivalryPlayer player2, WonLost wonLost, MatchInfo lastMatch) {
		super(player1, player2, wonLost, lastMatch);
	}

	public WonLost getWonLost() {
		return wonLost;
	}

	@Override HeadsToHeadsRivalry inverted() {
		return new HeadsToHeadsRivalry(player2, player1, wonLost.inverted(), lastMatch);
	}
}
