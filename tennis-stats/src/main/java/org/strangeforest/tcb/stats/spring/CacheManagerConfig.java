package org.strangeforest.tcb.stats.spring;

import java.time.*;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.*;
import org.springframework.cache.guava.*;
import org.springframework.context.annotation.*;

import com.google.common.cache.*;

@Configuration
public class CacheManagerConfig {

	@Value("${cache.max-size:1000}")
	private int cacheMaxSize;

	@Value("${cache.expiry-period:PT15M}")
	private Duration cacheExpiryPeriod;

	@Bean
	public CacheManager cacheManager() {
		GuavaCacheManager manager = new GuavaCacheManager();
		manager.setCacheBuilder(CacheBuilder.newBuilder()
			.maximumSize(cacheMaxSize)
			.expireAfterWrite(cacheExpiryPeriod.getSeconds(), TimeUnit.SECONDS)
		);
		return manager;
	}
}
