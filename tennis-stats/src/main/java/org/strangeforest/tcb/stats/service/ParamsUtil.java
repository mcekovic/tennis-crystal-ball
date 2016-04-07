package org.strangeforest.tcb.stats.service;

import org.springframework.jdbc.core.namedparam.*;

public abstract class ParamsUtil {

	public static MapSqlParameterSource params(String name, Object value) {
		return new MapSqlParameterSource(name, value);
	}
}
