package org.strangeforest.tcb.stats.controller;

import java.util.*;

import org.springframework.web.bind.annotation.*;

import com.google.common.collect.*;

public abstract class BaseController {

	public static final Map<String, String> VERSIONS = ImmutableMap.of(
		"jquery", "2.2.4",
		"jquery-ui", "1.11.4",
		"bootstrap", "3.3.7",
		"bootgrid", "1.3.1"
	);

	@ModelAttribute("versions")
	public Map<String, String> getVersions() {
		return VERSIONS;
	}
}
