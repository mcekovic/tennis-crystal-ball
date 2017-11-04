package org.strangeforest.tcb.util;

import java.time.*;
import java.util.*;

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
}
