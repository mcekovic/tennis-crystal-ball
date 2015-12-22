package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.function.*;

import org.postgresql.core.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.util.*;

@Service
public class DataService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String DB_SERVER_VERSION_QUERY = "SELECT version()";

	private static final String LAST_UPDATE_QUERY =
		"SELECT max(last_update) FROM (\n" +
		"  SELECT max(date) last_update FROM tournament_event\n" +
		"  UNION ALL\n" +
		"  SELECT max(rank_date) FROM player_ranking\n" +
		") AS last_update";

	private final Supplier<Integer> dbServerVersion = Memoizer.of(() -> dbServerVersion().getVersionNum());

	private Version dbServerVersion() {
		String versionStr = jdbcTemplate.queryForObject(DB_SERVER_VERSION_QUERY, String.class);
		String[] versionArr = versionStr.split(" ");
		if (versionArr.length >= 2) {
			String version = versionArr[1];
			if (version.endsWith(","))
				version = version.substring(0, version.length() - 1);
			return ServerVersion.from(version);
		}
		else
			return ServerVersion.v9_4;
	}

	public int getDBServerVersion() {
		return dbServerVersion.get();
	}

	@Cacheable(value = "Global", key = "'LastUpdate'")
	public Date getLastUpdate() {
		return jdbcTemplate.queryForObject(LAST_UPDATE_QUERY, Date.class);
	}
}
