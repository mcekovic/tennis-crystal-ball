package org.strangeforest.tcb.stats.util;

import static java.lang.Character.*;

public abstract class NameUtil {

	public static String shortenName(String name) {
		var sb = new StringBuilder(name.length());
		var words = name.split(" ");
		for (var i = 0; i < words.length; i++) {
			if (i > 0)
				sb.append(' ');
			if (i < words.length - 1)
				appendInitial(sb, words[i]);
			else
				sb.append(words[i]);
		}
		return sb.toString();
	}

	private static void appendInitial(StringBuilder sb, String name) {
		sb.append(toUpperCase(name.charAt(0)));
		sb.append('.');
	}
}
