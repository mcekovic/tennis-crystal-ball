package org.strangeforest.tcb.stats.spring;

import java.lang.management.*;
import java.util.*;

import org.apache.catalina.startup.*;
import org.springframework.boot.*;
import org.springframework.boot.actuate.info.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import com.google.common.collect.*;

@Component
@Profile("!dev")
public class RuntimeInfoContributor implements InfoContributor {

	@Override public void contribute(Info.Builder builder) {
		builder.withDetail("runtime", ImmutableMap.of(
			"jvm.version", Optional.ofNullable(getVersion(Runtime.class)).orElse(ManagementFactory.getRuntimeMXBean().getVmVersion()), // First for JDK 1.8, second for JDK 9
			"spring-boot.version", getVersion(SpringApplication.class),
			"spring.version", getVersion(ApplicationContext.class),
			"tomcat.version", getVersion(Tomcat.class)
		));
	}

	private String getVersion(Class<?> cls) {
		return cls.getPackage().getImplementationVersion();
	}
}
