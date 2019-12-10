package org.strangeforest.tcb.stats;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cache.annotation.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
public class TennisStatsApplication {

	private static volatile ConfigurableApplicationContext context;

	public static void main(String[] args) {
		run(args);
	}

	public static void restart() {
		String[] args = context.getBean(ApplicationArguments.class).getSourceArgs();
		Thread thread = new Thread(() -> {
			context.close();
			run(args);
		}, "Application restarter");
		thread.setDaemon(false);
		thread.start();
	}

	private static void run(String[] args) {
		context = SpringApplication.run(TennisStatsApplication.class, args);
	}
}
