package org.strangeforest.tcb.stats.web;

import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.*;

@TestConfiguration
@PropertySource("/visitors-test.properties")
@EnableTransactionManagement
@Import(DataSourceITConfig.class)
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
