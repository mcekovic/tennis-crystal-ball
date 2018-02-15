package org.strangeforest.tcb.stats.model.records;

import java.util.*;
import java.util.regex.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;

public abstract class RecordCategory {

	private final String id;
	private final String name;
	private final List<Record> records;

	protected static final String N_A = "";
	protected static final String TOURNAMENT = "Tournament";
	protected static final String NO_1 = "No1";
	protected static final String NO_2 = "No2";
	protected static final String NO_3 = "No3";
	protected static final String TOP_2 = "Top2";
	protected static final String TOP_3 = "Top3";
	protected static final String TOP_5 = "Top5";
	protected static final String TOP_10 = "Top10";
	protected static final String TOP_20 = "Top20";

	protected static final String NO_1_NAME = "No. 1";
	protected static final String NO_2_NAME = "No. 2";
	protected static final String NO_3_NAME = "No. 3";
	protected static final String TOP_2_NAME = "Top 2";
	protected static final String TOP_3_NAME = "Top 3";
	protected static final String TOP_5_NAME = "Top 5";
	protected static final String TOP_10_NAME = "Top 10";
	protected static final String TOP_20_NAME = "Top 20";
	protected static final String ATP = "ATP";
	protected static final String ELO = "Elo";

	protected static final String ALL_TOURNAMENTS = "level IN ('G', 'F', 'L', 'M', 'O', 'A', 'B')";
	protected static final String TITLES = "result = 'W'";
	protected static final String FINALS = "result >= 'F'";
	protected static final String MEDALS = "result >= 'BR'";
	protected static final String SEMI_FINALS = "result >= 'SF'";
	protected static final String QUARTER_FINALS = "result >= 'QF'";
	protected static final String ENTRIES = "result IS NOT NULL";
	protected static final String NO_1_RANK = "= 1";
	protected static final String NO_2_RANK = "= 2";
	protected static final String TOP_2_RANK = "<= 2";
	protected static final String NO_3_RANK = "= 3";
	protected static final String TOP_3_RANK = "<= 3";
	protected static final String TOP_5_RANK = "<= 5";
	protected static final String TOP_10_RANK = "<= 10";
	protected static final String TOP_20_RANK = "<= 20";

	private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^\\w]");

	protected RecordCategory(String name) {
		this.id = NON_WORD_PATTERN.matcher(name).replaceAll("");
		this.name = name;
		records = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Record> getRecords() {
		return records;
	}

	protected final void register(Record record) {
		records.add(record);
	}

	protected static String prefix(String s, String preffix) {
		return !Strings.isNullOrEmpty(s) ? preffix + s : s;
	}

	protected static String suffix(String s, String suffix) {
		return !Strings.isNullOrEmpty(s) ? s + suffix : s;
	}

	protected static String surfaceTournaments(String surface, String prefix) {
		return "surface = '" + surface + "' AND " + prefix + ALL_TOURNAMENTS;
	}

	protected static String indoorTournaments(boolean indoor, String prefix) {
		return "indoor = " + indoor + " AND " + prefix + ALL_TOURNAMENTS;
	}

	protected static String where(String condition) {
		return where(condition, 0);
	}

	protected static String where(String condition, int indent) {
		return !isNullOrEmpty(condition) ? "\n" + (indent > 0 ? repeat(" ", indent) : "") + "WHERE " + condition : "";
	}
}
