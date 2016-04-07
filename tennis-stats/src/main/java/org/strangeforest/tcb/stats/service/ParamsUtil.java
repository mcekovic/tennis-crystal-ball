package org.strangeforest.tcb.stats.service;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.collect.*;

public abstract class ParamsUtil {

	public static MapSqlParameterSource params(String name, Object value) {
		return new MapSqlParameterSource(name, value);
	}

	public static <T extends Comparable> void addParams(MapSqlParameterSource params, Range<T> range, String name) {
		if (range.hasLowerBound())
			params.addValue(name + "From", range.lowerEndpoint());
		if (range.hasUpperBound())
			params.addValue(name + "To", range.upperEndpoint());
	}
}
