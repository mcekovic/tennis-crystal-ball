package org.strangeforest.tcb.stats;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cache.annotation.*;
import org.springframework.scheduling.annotation.*;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class TennisStatsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TennisStatsApplication.class, args);
	}
}
