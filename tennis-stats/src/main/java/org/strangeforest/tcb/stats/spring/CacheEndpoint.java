package org.strangeforest.tcb.stats.spring;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

import com.google.common.base.*;

import static java.lang.String.*;

@Component @Endpoint(id = "cache")
@Profile("!dev")
public class CacheEndpoint {

	@Autowired private DataService dataService;

	@WriteOperation
	public String clearCache(@Selector String name) {
		if (Strings.isNullOrEmpty(name))
			name = ".*";
		int cacheCount = dataService.clearCaches(name);
		return format("OK (%1$d caches cleared)", cacheCount);
	}
}