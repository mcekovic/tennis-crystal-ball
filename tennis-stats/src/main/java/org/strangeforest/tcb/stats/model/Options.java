package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Arrays.*;

public abstract class Options {

	public static final List<Option> MAIN_TOURNAMENT_LEVELS = asList(
		new Option("G", "Grand Slam"),
		new Option("F", "Tour Finals"),
		new Option("M", "Masters"),
		new Option("A", "ATP"),
		new Option("O", "Olympics")
	);

	public static final List<Option> TOURNAMENT_LEVELS = new ArrayList<Option>(MAIN_TOURNAMENT_LEVELS) {{
		add(new Option("D", "Davis Cup"));
	}};

	public static final List<Option> ALL_TOURNAMENT_LEVELS = new ArrayList<Option>(TOURNAMENT_LEVELS) {{
		add(new Option("T", "World Team Cup"));
		add(new Option("H", "Others"));
	}};

	public static final List<Option> SURFACES = asList(
		new Option("H", "Hard"),
		new Option("C", "Clay"),
		new Option("G", "Grass"),
		new Option("P", "Carpet")
	);
}
