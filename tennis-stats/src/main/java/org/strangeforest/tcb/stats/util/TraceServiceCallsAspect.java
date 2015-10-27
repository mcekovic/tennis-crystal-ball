package org.strangeforest.tcb.stats.util;

import org.aopalliance.aop.*;
import org.springframework.aop.*;
import org.springframework.aop.interceptor.*;
import org.springframework.aop.support.*;
import org.springframework.aop.support.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

@Configuration
public class TraceServiceCallsAspect {

	@Bean
	public Advice traceInterceptor() {
		CustomizableTraceInterceptor traceInterceptor = new CustomizableTraceInterceptor();
		traceInterceptor.setUseDynamicLogger(true);
		traceInterceptor.setEnterMessage("$[methodName]($[arguments])");
		traceInterceptor.setExitMessage("~$[methodName]=$[returnValue] ($[invocationTime])");
		traceInterceptor.setExceptionMessage("!$[methodName]");
		return traceInterceptor;
	}

	@Bean
	public Advisor serviceTraceAdvisor() {
		Pointcut servicePointcut = new AnnotationMatchingPointcut(Service.class);
		return new DefaultPointcutAdvisor(servicePointcut, traceInterceptor());
	}
}
