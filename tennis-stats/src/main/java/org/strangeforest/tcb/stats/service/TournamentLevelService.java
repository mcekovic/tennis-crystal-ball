package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.stats.service.ResultSetUtil.*;

@Service
public class TournamentLevelService {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String TIMELINE_QUERY = //language=SQL
		"SELECT e.tournament_id, t.name, e.tournament_event_id, e.season, e.date, e.surface,\n" +
		"  m.winner_id, pw.%1$s winner_name, pw.name winner_full_name, pw.country_id winner_country_id, pw.active winner_active, m.winner_seed, m.winner_entry,\n" +
		"  m.loser_id runner_up_id, pl.%1$s runner_up_name, pl.name runner_up_full_name, pl.country_id runner_up_country_id, pl.active runner_up_active, m.loser_seed runner_up_seed, m.loser_entry runner_up_entry, m.score, m.outcome\n" +
		"FROM tournament_event e\n" +
		"INNER JOIN tournament t USING (tournament_id)\n" +
		"LEFT JOIN match m ON m.tournament_event_id = e.tournament_event_id AND m.round = 'F'\n" +
		"INNER JOIN player_v pw ON pw.player_id = m.winner_id\n" +
		"INNER JOIN player_v pl ON pl.player_id = m.loser_id\n" +
		"WHERE e.level = :level::tournament_level\n" +
		"ORDER BY e.season, e.date";

	private static final String TEAM_TIMELINE_QUERY =
		"SELECT w.season, e.tournament_event_id, e.surface, w.winner_id, w.runner_up_id, w.score\n" +
		"FROM team_tournament_event_winner w\n" +
		"LEFT JOIN tournament_event e ON e.level = w.level AND e.season = w.season\n" +
		"WHERE w.level = :level::tournament_level\n" +
		"AND (e.level <> 'D' OR e.name LIKE '%WG')\n" +
		"ORDER BY w.season DESC, e.date DESC";


	@Cacheable("TournamentLevelTimeline")
	public TournamentLevelTimeline getTournamentLevelTimeline(String level, boolean fullName) {
		TournamentLevelTimeline timeline = new TournamentLevelTimeline();
		fetchTournamentLevelTimeline(timeline, level, fullName);
		timeline.addMissingSeasonLastTournaments();
		return timeline;
	}

	@Cacheable("TournamentLevelsTimeline")
	public TournamentLevelTimeline getTournamentLevelGroupTimeline(TournamentLevelGroup levelGroup, boolean fullName) {
		TournamentLevelTimeline timeline = new TournamentLevelTimeline();
		for (TournamentLevel level : levelGroup.getLevels())
			fetchTournamentLevelTimeline(timeline, level.getCode(), fullName);
		return timeline;
	}

	private void fetchTournamentLevelTimeline(TournamentLevelTimeline timeline, String level, boolean fullName) {
		jdbcTemplate.query(
			String.format(TIMELINE_QUERY, fullName ? "name" : "last_name"),
			params("level", level),
			rs -> {
				TournamentLevelTimelineItem item = new TournamentLevelTimelineItem(
					rs.getInt("tournament_id"),
					rs.getString("name"),
					rs.getInt("season"),
					rs.getInt("tournament_event_id"),
					getLocalDate(rs, "date"),
					level,
					getInternedString(rs, "surface")
				);
				item.setWinner(mapPlayer(rs, "winner_", 1));
				item.setRunnerUp(mapPlayer(rs, "runner_up_", 2));
				item.setScore(rs.getString("score"));
				item.setOutcome(getInternedString(rs, "outcome"));
				timeline.addItem(item);
			}
		);
	}

	private TournamentLevelTimelinePlayer mapPlayer(ResultSet rs, String prefix, int rank) throws SQLException {
		TournamentLevelTimelinePlayer player = new TournamentLevelTimelinePlayer(rank,
			rs.getInt(prefix + "id"),
			rs.getString(prefix + "name"),
			getInternedString(rs, prefix + "country_id"),
			rs.getBoolean(prefix + "active")
		);
		player.setFullName(rs.getString(prefix + "full_name"));
		player.setSeed(getInteger(rs, prefix + "seed"));
		player.setEntry(getInternedString(rs, prefix + "entry"));
		return player;
	}


	@Cacheable("TeamTournamentLevelTimeline")
	public List<TeamTournamentLevelTimelineItem> getTeamTournamentLevelTimeline(String level) {
		return jdbcTemplate.query(
			TEAM_TIMELINE_QUERY,
			params("level", level),
			(rs, rowNum) -> {
				TeamTournamentLevelTimelineItem item = new TeamTournamentLevelTimelineItem(
					rs.getInt("season"),
					rs.getInt("tournament_event_id"),
					getInternedString(rs, "surface")
				);
				item.setWinnerId(getInternedString(rs, "winner_id"));
				item.setRunnerUpId(getInternedString(rs, "runner_up_id"));
				item.setScore(rs.getString("score"));
				return item;
			}
		);
	}
}


