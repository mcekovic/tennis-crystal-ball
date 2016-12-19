package org.strangeforest.tcb.stats.prediction;

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
@ComponentScan("org.strangeforest.tcb.stats.service.prediction")
public class PredictionITsConfig {

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public PlayerService playerService() {
		return new PlayerService();
	}
}
