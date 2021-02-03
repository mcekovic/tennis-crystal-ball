package org.strangeforest.tcb.stats.spring;

import org.aopalliance.aop.*;
import org.springframework.aop.*;
import org.springframework.aop.interceptor.*;
import org.springframework.aop.support.*;
import org.springframework.aop.support.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import static org.springframework.beans.factory.config.BeanDefinition.*;

@Configuration @Role(ROLE_INFRASTRUCTURE)
@ConditionalOnProperty("tennis-stats.enable-tracing")
public class TraceServiceCallsAspect {

	@Bean @Role(ROLE_INFRASTRUCTURE)
	public Advice traceInterceptor() {
		var traceInterceptor = new CustomizableTraceInterceptor();
		traceInterceptor.setUseDynamicLogger(true);
		traceInterceptor.setEnterMessage("$[methodName]($[arguments])");
		traceInterceptor.setExitMessage("~$[methodName]=$[returnValue] ($[invocationTime])");
		traceInterceptor.setExceptionMessage("!$[methodName]");
		return traceInterceptor;
	}

	@Bean @Role(ROLE_INFRASTRUCTURE)
	public Advisor serviceTraceAdvisor() {
		Pointcut servicePointcut = new AnnotationMatchingPointcut(Service.class);
		return new DefaultPointcutAdvisor(servicePointcut, traceInterceptor());
	}
}
