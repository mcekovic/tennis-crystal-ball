package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Arrays.*;

public abstract class Options {

	public static final List<Option> MAIN_TOURNAMENT_LEVELS = asList(
		new Option("G", "Grand Slam"),
		new Option("F", "Tour Finals"),
		new Option("M", "Masters"),
		new Option("O", "Olympics"),
		new Option("A", "ATP 500"),
		new Option("B", "ATP 250")
	);

	public static final List<Option> TOURNAMENT_LEVELS = new ArrayList<Option>(MAIN_TOURNAMENT_LEVELS) {{
		add(new Option("D", "Davis Cup"));
	}};

	public static final List<Option> ALL_TOURNAMENT_LEVELS = new ArrayList<Option>(MAIN_TOURNAMENT_LEVELS) {{
		add(new Option("H", "Others"));
		add(new Option("D", "Davis Cup"));
		add(new Option("T", "Others Team"));
	}};

	public static final List<Option> SURFACES = asList(
		new Option("H", "Hard"),
		new Option("C", "Clay"),
		new Option("G", "Grass"),
		new Option("P", "Carpet")
	);
}
