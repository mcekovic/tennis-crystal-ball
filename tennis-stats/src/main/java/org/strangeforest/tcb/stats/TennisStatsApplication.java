package org.strangeforest.tcb.stats;

import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cache.*;
import org.springframework.cache.annotation.*;
import org.springframework.cache.guava.*;
import org.springframework.context.annotation.*;

import com.google.common.cache.*;

@SpringBootApplication
@EnableCaching
public class TennisStatsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TennisStatsApplication.class, args);
	}

	@Value("${cache.max-size:1000}")
	private int cacheMaxSize;

	@Value("${cache.expiry-period:5}")
	private int cacheExpiryPeriod;

	@Value("${cache.expiry-period-unit:MINUTES}")
	private TimeUnit cacheExpiryPeriodUnit;

	@Bean
	public CacheManager cacheManager() {
		GuavaCacheManager manager = new GuavaCacheManager();
		manager.setCacheBuilder(CacheBuilder.newBuilder()
			.maximumSize(cacheMaxSize)
			.expireAfterWrite(cacheExpiryPeriod, cacheExpiryPeriodUnit)
		);
		return manager;
	}
}
