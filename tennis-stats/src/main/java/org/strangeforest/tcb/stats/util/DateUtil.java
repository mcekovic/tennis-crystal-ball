package org.strangeforest.tcb.stats.util;

import java.util.*;
import java.time.*;

public abstract class DateUtil {

	public static Date toDate(LocalDate date) {
		return date != null ? Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) : null;
	}

	public static LocalDate toLocalDate(java.sql.Date date) {
		return date != null ? date.toLocalDate() : null;
	}
}
