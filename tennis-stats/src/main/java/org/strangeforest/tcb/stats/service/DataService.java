package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.util.*;

@Service
public class DataService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String LAST_UPDATE_QUERY =
		"SELECT max(last_update) FROM (\n" +
		"  SELECT max(date) last_update FROM tournament_event\n" +
		"  UNION ALL\n" +
		"  SELECT max(rank_date) FROM player_ranking\n" +
		") AS last_update";

	private static final long LAST_UPDATE_EXPIRY_PERIOD = TimeUnit.MINUTES.toMillis(5L);

	private final Supplier<Date> lastUpdate =  Memoizer.of(
		() -> jdbcTemplate.queryForObject(LAST_UPDATE_QUERY, Date.class),
		LAST_UPDATE_EXPIRY_PERIOD
	);

	public Date getLastUpdate() {
		return lastUpdate.get();
	}
}
