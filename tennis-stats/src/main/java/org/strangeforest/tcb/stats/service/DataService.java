package org.strangeforest.tcb.stats.service;

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import org.postgresql.core.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.*;
import org.springframework.cache.annotation.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.util.*;

import static java.lang.String.*;

@Service
public class DataService {

	@Autowired private JdbcTemplate jdbcTemplate;
	@Autowired private CacheManager cacheManager;
	@Autowired private MatchPredictionService matchPredictionService;

	private static final String DB_SERVER_VERSION_QUERY = "SELECT version()";

	private static final String DATA_UPDATE_QUERY =
		"SELECT greatest(\n" +
		"  (SELECT max(tournament_end(date, level, draw_size)) data_update FROM tournament_event),\n" +
		"  (SELECT max(rank_date) FROM player_ranking)\n" +
		")";

	private static final String SEASONS_QUERY =
		"SELECT DISTINCT season FROM tournament_event ORDER BY season DESC";

	private final Supplier<String> dbServerVersionString = Memoizer.of(this::dbServerVersionString);
	private final Supplier<Version> dbServerVersion = Memoizer.of(this::dbServerVersion);

	private String dbServerVersionString() {
		return jdbcTemplate.queryForObject(DB_SERVER_VERSION_QUERY, String.class);
	}

	private Version dbServerVersion() {
		String versionStr = getDBServerVersionString();
		String[] versionArr = versionStr.split(" ");
		if (versionArr.length >= 2) {
			String version = versionArr[1];
			if (version.endsWith(","))
				version = version.substring(0, version.length() - 1);
			return ServerVersion.from(version);
		}
		else
			return ServerVersion.v9_5;
	}

	public String getDBServerVersionString() {
		return dbServerVersionString.get();
	}

	public int getDBServerVersion() {
		return dbServerVersion.get().getVersionNum();
	}

	public String getDatabaseSize(String databaseName) {
		return jdbcTemplate.queryForObject(format("SELECT pg_size_pretty(pg_database_size('%1$s'))", databaseName), String.class);
	}

	@Cacheable(value = "Global", key = "'DataUpdate'")
	public LocalDate getDataUpdate() {
		return jdbcTemplate.queryForObject(DATA_UPDATE_QUERY, LocalDate.class);
	}

	@Cacheable(value = "Global", key = "'Seasons'")
	public List<Integer> getSeasons() {
		return jdbcTemplate.queryForList(SEASONS_QUERY, Integer.class);
	}

	public Integer getFirstSeason() {
		List<Integer> seasons = getSeasons();
		return !seasons.isEmpty() ? seasons.get(seasons.size() - 1) : null;
	}

	public Integer getLastSeason() {
		List<Integer> seasons = getSeasons();
		return !seasons.isEmpty() ? seasons.get(0) : null;
	}

	public int clearCaches(String nameRegEx) {
		Pattern pattern = Pattern.compile(nameRegEx);
		int cacheCount = 0;
		for (String cacheName : cacheManager.getCacheNames()) {
			if (pattern.matcher(cacheName).matches()) {
				cacheManager.getCache(cacheName).clear();
				cacheCount++;
			}
		}
		matchPredictionService.clearCache();
		return cacheCount;
	}

	public void evictGlobal(Object key) {
		Cache cache = cacheManager.getCache("Global");
		if (cache != null)
			cache.evict(key);
	}

	private static final int CONNECT_TIMEOUT = 10000;
	private static final int READ_TIMEOUT = 10000;

	public int checkURL(String url) throws IOException {
		HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
		con.setRequestMethod(HttpMethod.HEAD.name());
		con.setConnectTimeout(CONNECT_TIMEOUT);
		con.setReadTimeout(READ_TIMEOUT);
		return con.getResponseCode();
	}
}
