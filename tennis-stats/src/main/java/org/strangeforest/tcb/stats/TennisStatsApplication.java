package org.strangeforest.tcb.stats;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cache.annotation.*;
import org.springframework.context.*;
import org.springframework.scheduling.annotation.*;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class TennisStatsApplication {

	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		setContext(run(args));
	}

	public static synchronized void restart() {
		String[] args = context.getBean(ApplicationArguments.class).getSourceArgs();
		Thread thread = new Thread(() -> {
			context.close();
			context = run(args);
		}, "Application restarter");
		thread.setDaemon(false);
		thread.start();
	}

	private static ConfigurableApplicationContext run(String[] args) {
		return SpringApplication.run(TennisStatsApplication.class, args);
	}

	private static synchronized void setContext(ConfigurableApplicationContext ctx) {
		context = ctx;
	}
}
