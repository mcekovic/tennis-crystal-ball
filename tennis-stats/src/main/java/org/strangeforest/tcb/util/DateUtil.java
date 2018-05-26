package org.strangeforest.tcb.util;

import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

import com.google.common.collect.*;

public abstract class DateUtil {

	public static final String DATE_FORMAT = "dd-MM-yyyy";

	public static Date toDate(LocalDate date) {
		return date != null ? Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) : null;
	}

	public static LocalDate toLocalDate(Date date) {
		if (date instanceof java.sql.Date)
			return ((java.sql.Date)date).toLocalDate();
		else
			return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
	}

	public static String formatDate(LocalDate date) {
		return DateTimeFormatter.ofPattern(DATE_FORMAT).format(date);
	}

	public static String formatDate(Date date) {
		return new SimpleDateFormat(DATE_FORMAT).format(date);
	}

	public static Range<LocalDate> toDateRange(Integer fromYear, Integer toYear) {
		return RangeUtil.toRange(fromYear != null ? LocalDate.of(fromYear, 1, 1) : null, toYear != null ? LocalDate.of(toYear, 12, 31) : null);
	}
}
