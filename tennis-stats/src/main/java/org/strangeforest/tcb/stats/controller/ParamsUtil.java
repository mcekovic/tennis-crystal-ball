package org.strangeforest.tcb.stats.controller;

import java.io.*;
import java.time.*;
import java.util.*;

import org.springframework.web.util.*;

import static com.google.common.base.Strings.*;
import static java.util.Collections.*;
import static org.strangeforest.tcb.util.DateUtil.*;

public class ParamsUtil {

	public static final ParamsUtil INSTANCE = new ParamsUtil();

	public static final String F = "false";
	public static final String T = "true";

	private static final String ENCODING = "UTF-8";
	private static final String EMPTY = "\"\"";

	public String param(String name, Object value) throws UnsupportedEncodingException {
		if (value != null) {
			if (value instanceof String) {
				if (((String)value).length() > 0)
					return format(name, value);
			}
			else if (value instanceof LocalDate)
				return format(name, formatDate((LocalDate)value));
			else
				return format(name, value);
		}
		return EMPTY;
	}

	private String format(String name, Object value) throws UnsupportedEncodingException {
		return "\"&" + name + '=' + UriUtils.encode(value.toString(), ENCODING) + "\"";
	}

	public static Map<String, Integer> parseIntProperties(String props) {
		if (isNullOrEmpty(props))
			return emptyMap();
		Map<String, Integer> propMap = new HashMap<>();
		for (var prop : props.split(",")) {
			var pos = prop.indexOf('=');
			if (pos > 0)
				propMap.put(prop.substring(0, pos), Integer.parseInt(prop.substring(pos + 1)));
			else
				throw new IllegalArgumentException("Invalid property definition: " + prop);
		}
		return propMap;
	}
}
