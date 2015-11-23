package org.strangeforest.tcb.stats.util;

import java.util.*;
import java.time.*;

import com.google.common.collect.*;

public abstract class DateUtil {

	public static Date toDate(LocalDate date) {
		return date != null ? Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) : null;
	}

	public static LocalDate toLocalDate(Date date) {
		if (date instanceof java.sql.Date)
			return ((java.sql.Date)date).toLocalDate();
		else
			return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
	}

	public static Range<LocalDate> toRange(LocalDate fromDate, LocalDate toDate) {
		if (fromDate != null)
			return toDate != null ? Range.closed(fromDate, toDate) : Range.atLeast(fromDate);
		else
			return toDate != null ? Range.atMost(toDate) : Range.all();
	}
}
