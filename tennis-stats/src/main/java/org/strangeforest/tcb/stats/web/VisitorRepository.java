package org.strangeforest.tcb.stats.web;

import java.math.*;
import java.sql.*;
import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.util.*;

import eu.bitwalker.useragentutils.*;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Repository @VisitorSupport
public class VisitorRepository {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String FIND = // language=SQL
		"SELECT visitor_id, country_id, country, agent_type, hits, last_hit FROM visitor\n" +
		"WHERE ip_address = :ipAddress AND active ORDER BY last_hit DESC";

	private static final String FIND_ALL = // language=SQL
		"SELECT max(visitor_id) AS visitor_id, ip_address, country_id, country, agent_type, sum(hits) AS hits, max(last_hit) AS last_hit FROM visitor\n" +
		"WHERE active GROUP by ip_address, country_id, country, agent_type";

	private static final String CREATE = // language=SQL
		"INSERT INTO visitor (ip_address, country_id, country, agent_type, hits, last_hit)\n" +
		"VALUES (:ipAddress, :countryId, :country, :agentType, :hits, :lastHit)";

	private static final String SAVE = // language=SQL
		"UPDATE visitor SET hits = :hits, last_hit = :lastHit%1$s\n" +
		"WHERE visitor_id = :visitorId";

	private static final String STATS_QUERY = // language=SQL
		"SELECT %1$s AS value FROM visitor\n" +
		"WHERE last_hit >= now() - INTERVAL '%2$s'%3$s";

	private static final String STATS_BY_QUERY = // language=SQL
		"SELECT %1$s, %2$s AS value FROM visitor\n" +
		"WHERE last_hit >= now() - INTERVAL '%3$s'%4$s\n" +
		"GROUP BY %1$s HAVING %2$s > 0 ORDER BY value DESC";

	private static final String NO_ROBOTS_CONDITION = // language=SQL
		" AND agent_type <> 'ROBOT'";


	// CRUD

	public Optional<Visitor> find(String ipAddress) {
		return jdbcTemplate.query(FIND, params("ipAddress", ipAddress),
			(rs, rowNum) -> mapVisitor(ipAddress, rs)
		).stream().findFirst();
	}

	public List<Visitor> findAll() {
		return jdbcTemplate.query(FIND_ALL, (rs, rowNum) -> mapVisitor(rs.getString("ip_address"), rs));
	}

	private static Visitor mapVisitor(String ipAddress, ResultSet rs) throws SQLException {
		return new Visitor(
			rs.getLong("visitor_id"),
			ipAddress,
			rs.getString("country_id"),
			rs.getString("country"),
			rs.getString("agent_type"),
			rs.getInt("hits"),
			rs.getTimestamp("last_hit").toInstant()
		);
	}

	public Visitor create(String ipAddress, String countryId, String country, String agentType) {
		int hits = 1;
		Instant lastHit = Instant.now();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(CREATE,
			params("ipAddress", ipAddress)
				.addValue("countryId", countryId)
				.addValue("country", country)
				.addValue("agentType", agentType)
				.addValue("hits", hits)
				.addValue("lastHit", Timestamp.from(lastHit)),
		keyHolder, new String[] {"visitor_id"});
		return new Visitor(keyHolder.getKey().longValue(), ipAddress, countryId, country, agentType, hits, lastHit);
	}

	public void save(Visitor visitor) {
		save(visitor, false);
	}

	public void expire(Visitor visitor) {
		save(visitor, true);
	}

	private void save(Visitor visitor, boolean expire) {
		jdbcTemplate.update(format(SAVE, expire ? ", active = FALSE" : ""), saveVisitorParams(visitor));
	}

	public void saveAll(Collection<Visitor> visitors) {
		jdbcTemplate.batchUpdate(
			format(SAVE, ""),
			visitors.stream().map(VisitorRepository::saveVisitorParams).collect(toList()).toArray(new SqlParameterSource[visitors.size()])
		);
	}

	private static MapSqlParameterSource saveVisitorParams(Visitor visitor) {
		return params("visitorId", visitor.getId())
			.addValue("hits", visitor.getHits())
			.addValue("lastHit", Timestamp.from(visitor.getLastHit()));
	}


	// Queries

	public BigDecimal getVisitors(VisitorStat stat, VisitorInterval interval, boolean robots) {
		return jdbcTemplate.getJdbcOperations().queryForObject(
			format(STATS_QUERY, stat.getExpression(), interval.getExpression(), robotsCondition(robots)),
			(rs, rowNum) -> rs.getBigDecimal("value")
		);
	}

	public Map<String, BigDecimal> getVisitorsByCountry(VisitorStat stat, VisitorInterval interval, boolean robots) {
		return replaceNullKey(getVisitorsBy("country", stat, interval, robots), Country.UNKNOWN_NAME);
	}

	public Map<String, BigDecimal> getVisitorsByAgentType(VisitorStat stat, VisitorInterval interval, boolean robots) {
		return replaceNullKey(getVisitorsBy("agent_type", stat, interval, robots), BrowserType.UNKNOWN.name());
	}

	private Map<String, BigDecimal> getVisitorsBy(String dimension, VisitorStat stat, VisitorInterval interval, boolean robots) {
		Map<String, BigDecimal> visitorsMap = new LinkedHashMap<>();
		jdbcTemplate.getJdbcOperations().query(
			format(STATS_BY_QUERY, dimension, stat.getExpression(), interval.getExpression(), robotsCondition(robots)),
			rs -> {
				String country = rs.getString(dimension);
				BigDecimal value = rs.getBigDecimal("value");
				visitorsMap.put(country, value);
			}
		);
		return visitorsMap;
	}

	private static String robotsCondition(boolean robots) {
		return robots ? "" : NO_ROBOTS_CONDITION;
	}

	private static <K, V> Map<K, V> replaceNullKey(Map<K, V> map, K nullKey) {
		if (map.containsKey(null))
			map.put(nullKey, map.remove(null));
		return map;
	}
}
