package org.strangeforest.tcb.stats.web;

import java.sql.*;
import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.*;

import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;

@Repository
public class VisitorRepository {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String FIND = "SELECT visitor_id, country_id, visits, last_visit FROM visitor WHERE ip_address = :ipAddress AND active";
	private static final String FIND_ALL = "SELECT visitor_id, country_id, ip_address, visits, last_visit FROM visitor WHERE active";
	private static final String CREATE = "INSERT INTO visitor (ip_address, country_id, visits, last_visit) VALUES (:ipAddress, :countryId, :visits, :lastVisit)";
	private static final String SAVE = "UPDATE visitor SET visits = :visits, last_visit = :lastVisit WHERE visitor_id = :visitorId";
	private static final String EXPIRE = "UPDATE visitor SET active = FALSE WHERE visitor_id = :visitorId";

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
			rs.getInt("visits"),
			rs.getTimestamp("last_visit").toInstant()
		);
	}

	public Visitor create(String ipAddress, String countryId) {
		int visits = 1;
		Instant lastVisit = Instant.now();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(CREATE,
			params("ipAddress", ipAddress)
				.addValue("countryId", countryId)
				.addValue("visits", visits)
				.addValue("lastVisit", Timestamp.from(lastVisit)),
		keyHolder, new String[] {"visitor_id"});
		return new Visitor(keyHolder.getKey().longValue(), ipAddress, countryId, visits, lastVisit);
	}

	public void save(Visitor visitor) {
		jdbcTemplate.update(SAVE,
			params("visitorId", visitor.getId())
				.addValue("visits", visitor.getVisits())
				.addValue("lastVisit", Timestamp.from(visitor.getLastVisit()))
		);
	}

	public void saveAll(Collection<Visitor> visitors) {
		jdbcTemplate.batchUpdate(SAVE, visitors.stream().map(visitor ->
			params("visitorId", visitor.getId())
				.addValue("visits", visitor.getVisits())
				.addValue("lastVisit", Timestamp.from(visitor.getLastVisit()))
		).collect(toList()).toArray(new SqlParameterSource[visitors.size()]));
	}

	public void expire(Visitor visitor) {
		jdbcTemplate.update(EXPIRE, params("visitorId", visitor.getId()));
	}
}
