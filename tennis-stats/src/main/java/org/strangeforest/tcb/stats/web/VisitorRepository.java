package org.strangeforest.tcb.stats.web;

import java.sql.*;
import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.*;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Repository @VisitorSupport
public class VisitorRepository {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String FIND = "SELECT visitor_id, country_id, country, hits, last_hit FROM visitor WHERE ip_address = :ipAddress AND active";
	private static final String FIND_ALL = "SELECT visitor_id, country_id, country, ip_address, hits, last_hit FROM visitor WHERE active";
	private static final String CREATE = "INSERT INTO visitor (ip_address, country_id, country, hits, last_hit) VALUES (:ipAddress, :countryId, :country, :hits, :lastHit)";
	private static final String SAVE = "UPDATE visitor SET hits = :hits, last_hit = :lastHit%1$s WHERE visitor_id = :visitorId";

	public Optional<Visitor> find(String ipAddress) {
		return jdbcTemplate.query(FIND, params("ipAddress", ipAddress), rs ->
			rs.next() ? Optional.of(mapVisitor(ipAddress, rs)) : Optional.<Visitor>empty()
		);
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
			rs.getInt("hits"),
			rs.getTimestamp("last_hit").toInstant()
		);
	}

	public Visitor create(String ipAddress, String countryId, String country) {
		int hits = 1;
		Instant lastHit = Instant.now();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(CREATE,
			params("ipAddress", ipAddress)
				.addValue("countryId", countryId)
				.addValue("country", country)
				.addValue("hits", hits)
				.addValue("lastHit", Timestamp.from(lastHit)),
		keyHolder, new String[] {"visitor_id"});
		return new Visitor(keyHolder.getKey().longValue(), ipAddress, countryId, country, hits, lastHit);
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
}
