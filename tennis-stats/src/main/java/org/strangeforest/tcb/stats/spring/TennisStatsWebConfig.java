package org.strangeforest.tcb.stats.spring;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.*;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import org.strangeforest.tcb.stats.controller.*;

@Configuration @ConditionalOnWebApplication
@EnableConfigurationProperties(ServerSSLProperties.class)
public class TennisStatsWebConfig implements WebMvcConfigurer {

	@Autowired(required = false) DownForMaintenanceInterceptor downForMaintenanceInterceptor;

	@Override public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new RequestURLLoggingHandlerInterceptor());
		if (downForMaintenanceInterceptor != null)
			registry.addInterceptor(downForMaintenanceInterceptor);
	}

	@Bean
	public ErrorAttributes errorAttributes() {
		return new TennisStatsErrorAttributes();
	}
}
