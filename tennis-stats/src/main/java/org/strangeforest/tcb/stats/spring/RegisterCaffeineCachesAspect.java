package org.strangeforest.tcb.stats.spring;

import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.metrics.cache.*;
import org.springframework.cache.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import static org.springframework.beans.factory.config.BeanDefinition.*;

@Aspect
@Component @Role(ROLE_INFRASTRUCTURE)
public class RegisterCaffeineCachesAspect {

	@Autowired private CacheMetricsRegistrar metricsRegistrar;

	@AfterReturning(pointcut="execution(* org.springframework.cache.caffeine.CaffeineCacheManager.getCache(java.lang.String))", returning="cache")
	public void registerCaffeineCache(Cache cache) {
		metricsRegistrar.bindCacheToRegistry(cache);
	}
}
