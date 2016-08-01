package org.strangeforest.tcb.stats.spring;

import java.sql.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

@Component
public class DataHealthIndicator extends AbstractHealthIndicator {

	@Autowired private DataService dataService;

	@Override protected void doHealthCheck(Health.Builder builder) throws Exception {
		Date lastUpdate = dataService.getLastUpdate();
		if (lastUpdate != null)
			builder.up().withDetail("lastUpdate", lastUpdate).build();
		else
			builder.down().build();
	}
}
