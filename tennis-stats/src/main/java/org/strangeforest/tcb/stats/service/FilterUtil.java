package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;

abstract class FilterUtil {

	static boolean stringsEqual(String s1, String s2) {
		return Objects.equals(emptyToNull(s1), emptyToNull(s2));
	}
}
