package org.strangeforest.tcb.stats.records;

import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.*;
import org.strangeforest.tcb.stats.service.*;

@TestConfiguration
@EnableTransactionManagement
@Import(DataSourceITConfig.class)
public class RecordsITsConfig {

	@Bean
	public RecordsService recordsService() {
		return new RecordsService();
	}

	@Bean
	public PlayerService playerService() {
		return new PlayerService();
	}
}
