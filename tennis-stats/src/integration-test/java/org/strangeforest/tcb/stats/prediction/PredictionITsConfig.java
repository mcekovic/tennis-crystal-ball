package org.strangeforest.tcb.stats.prediction;

import org.springframework.boot.test.context.*;
import org.springframework.cache.*;
import org.springframework.cache.caffeine.*;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.*;
import org.strangeforest.tcb.stats.service.*;
import org.strangeforest.tcb.stats.spring.*;

@TestConfiguration
@EnableTransactionManagement
@Import({TennisStatsConfig.class, DataSourceITConfig.class})
public class PredictionITsConfig {

	@Bean
	public MatchPredictionService matchPredictionService() {
		return new MatchPredictionService(false);
	}

	@Bean
	public PlayerService playerService() {
		return new PlayerService();
	}

	@Bean
	public DataService dataService() {
		return new DataService();
	}

	@Bean
	public CacheManager cacheManager() {
		return new CaffeineCacheManager();
	}
}
