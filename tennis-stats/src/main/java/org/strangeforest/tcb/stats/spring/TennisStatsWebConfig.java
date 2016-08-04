package org.strangeforest.tcb.stats.spring;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.web.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import org.strangeforest.tcb.stats.controller.*;

@Configuration
public class TennisStatsWebConfig extends WebMvcConfigurerAdapter {

	@Autowired DownForMaintenanceInterceptor downForMaintenanceInterceptor;

	@Override public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(downForMaintenanceInterceptor);
	}

	@Bean
	public static ErrorAttributes errorAttributes() {
		return new TennisStatsErrorAttributes();
	}
}
