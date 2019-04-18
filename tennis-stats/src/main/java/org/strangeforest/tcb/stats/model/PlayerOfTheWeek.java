package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.stats.model.core.*;

public class PlayerOfTheWeek {

	private final Player player;
	private final TournamentEvent title;

	public PlayerOfTheWeek(Player player) {
		this(player, null);
	}
	
	public PlayerOfTheWeek(Player player, TournamentEvent title) {
		this.player = player;
		this.title = title;
	}

	public Player getPlayer() {
		return player;
	}

	public TournamentEvent getTitle() {
		return title;
	}

	public boolean hasTitle() {
		return title != null;
	}
}
