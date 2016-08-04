package org.strangeforest.tcb.stats.records;

import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.context.support.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.spring.*;

@TestConfiguration
@EnableTransactionManagement
@Import({TennisStatsConfig.class, DataSourceITConfig.class})
public class RecordsITsConfig {

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public RecordsService recordsService() {
		return new RecordsService();
	}

	@Bean
	public PlayerService playerService() {
		return new PlayerService();
	}
}
