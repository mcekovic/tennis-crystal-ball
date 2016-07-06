package org.strangeforest.tcb.stats.spring;

import java.sql.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

@Component
public class DataHealthIndicator implements HealthIndicator {

	@Autowired private DataService dataService;

	@Override public Health health() {
		Date lastUpdate = dataService.getLastUpdate();
		if (lastUpdate != null)
			return Health.up().withDetail("lastUpdate", lastUpdate).build();
		else
			return Health.down().build();
	}
}
