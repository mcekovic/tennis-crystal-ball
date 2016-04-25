package org.strangeforest.tcb.stats.web;

import org.springframework.context.annotation.*;
import org.springframework.context.support.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.spring.*;

@Configuration
@PropertySource("/application-test.properties")
@EnableTransactionManagement
@Import({TennisStatsConfig.class, DataSourceITConfig.class})
public class VisitorITsConfig {

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

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
