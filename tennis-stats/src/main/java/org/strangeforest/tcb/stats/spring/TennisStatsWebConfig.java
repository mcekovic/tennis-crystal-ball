package org.strangeforest.tcb.stats.spring;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import org.strangeforest.tcb.stats.controller.*;

@Configuration @ConditionalOnWebApplication
public class TennisStatsWebConfig extends WebMvcConfigurerAdapter {

	@Autowired(required = false) DownForMaintenanceInterceptor downForMaintenanceInterceptor;

	@Override public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new RequestURLLoggingHandlerInterceptor());
		if (downForMaintenanceInterceptor != null)
			registry.addInterceptor(downForMaintenanceInterceptor);
	}

	@Bean
	public static ErrorAttributes errorAttributes() {
		return new TennisStatsErrorAttributes();
	}
}
