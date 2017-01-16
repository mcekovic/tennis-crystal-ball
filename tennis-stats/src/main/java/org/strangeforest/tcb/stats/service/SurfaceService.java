package org.strangeforest.tcb.stats.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

@Service
public class SurfaceService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String TIMELINE_QUERY = //language=SQL
		"SELECT e.season, count(m.match_id) AS match_count,\n" +
		"  sum(CASE m.surface WHEN 'H' THEN 1 ELSE 0 END) AS hard_match_count,\n" +
		"  sum(CASE m.surface WHEN 'C' THEN 1 ELSE 0 END) AS clay_match_count,\n" +
		"  sum(CASE m.surface WHEN 'G' THEN 1 ELSE 0 END) AS grass_match_count,\n" +
		"  sum(CASE m.surface WHEN 'P' THEN 1 ELSE 0 END) AS carpet_match_count\n" +
		"FROM match m\n" +
		"INNER JOIN tournament_event e USING(tournament_event_id)\n" +
		"GROUP BY e.season\n" +
		"ORDER BY e.season DESC";


	@Cacheable("SurfaceTimeline")
	public List<SurfaceTimelineItem> getSurfaceTimeline() {
		return jdbcTemplate.query(
			TIMELINE_QUERY,
			(rs, rowNum) -> new SurfaceTimelineItem(
				rs.getInt("season"),
				rs.getInt("match_count"),
				rs.getInt("hard_match_count"),
				rs.getInt("clay_match_count"),
				rs.getInt("grass_match_count"),
				rs.getInt("carpet_match_count")
			)
		);
	}
}


