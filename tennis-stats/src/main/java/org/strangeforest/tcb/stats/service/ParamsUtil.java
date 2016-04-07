package org.strangeforest.tcb.stats.service;

import org.springframework.jdbc.core.namedparam.*;

public abstract class ParamsUtil {

	public static MapSqlParameterSource param(String name, Object value) {
		return new MapSqlParameterSource(name, value);
	}

	public static MapSqlParameterSource params(MapSqlParameterSource params, String name, Object value) {
		return new MapSqlParameterSource(params.getValues())
			.addValue(name, value);
	}

	public static MapSqlParameterSource params(MapSqlParameterSource params, String name1, Object value1, String name2, Object value2, String name3, Object value3) {
		return new MapSqlParameterSource(params.getValues())
			.addValue(name1, value1)
			.addValue(name2, value2)
			.addValue(name3, value3);
	}
}
