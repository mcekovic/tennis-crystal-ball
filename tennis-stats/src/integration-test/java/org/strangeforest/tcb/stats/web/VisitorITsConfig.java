package org.strangeforest.tcb.stats.web;

import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.*;
import org.strangeforest.tcb.stats.spring.*;

@TestConfiguration
@PropertySource("/application-test.properties")
@EnableTransactionManagement
@Import({TennisStatsConfig.class, DataSourceITConfig.class})
public class VisitorITsConfig {

	@Bean
	public VisitorManager visitorManager() {
		return new VisitorManager();
	}

	@Bean
	public VisitorRepository visitorRepository() {
		return new VisitorRepository();
	}

	@Bean
	public GeoIPService geoIPService() {
		return new GeoIPService();
	}
}
