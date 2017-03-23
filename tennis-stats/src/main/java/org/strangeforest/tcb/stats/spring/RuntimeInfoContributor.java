package org.strangeforest.tcb.stats.spring;

import org.apache.catalina.startup.*;
import org.springframework.boot.*;
import org.springframework.boot.actuate.info.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

import com.google.common.collect.*;

@Component
public class RuntimeInfoContributor implements InfoContributor {

	@Override public void contribute(Info.Builder builder) {
		builder.withDetail("runtime", ImmutableMap.of(
			"jvm.version", getVersion(Runtime.class),
			"spring-boot.version", getVersion(SpringApplication.class),
			"spring.version", getVersion(ApplicationContext.class),
			"tomcat.version", getVersion(Tomcat.class)
		));
	}

	private String getVersion(Class<?> cls) {
		return cls.getPackage().getImplementationVersion();
	}
}
