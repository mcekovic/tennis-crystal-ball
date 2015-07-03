package org.strangeforest.tcb.stats.controler;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;

@RestController
public class GOATController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final int PLAYER_COUNT = 1000;

	private static final String GOAT_LIST_QUERY =
		"WITH goat_list AS (" +
			"SELECT player_id, rank() OVER (ORDER BY goat_points DESC NULLS LAST) AS goat_ranking, name, goat_points FROM player_v " +
			"ORDER BY goat_points DESC NULLS LAST, name LIMIT ? OFFSET ?" +
		"), titles AS (" +
			"SELECT player_id, level, count(*) AS titles FROM tournament_event_player_result " +
			"LEFT JOIN tournament_event USING (tournament_event_id) " +
			"WHERE result = 'W' " +
			"GROUP BY player_id, level" +
		") " +
		"SELECT goat_ranking, name, goat_points, " +
			"(SELECT titles FROM titles t WHERE t.player_id = g.player_id AND t.level = 'G') AS grand_slams, " +
			"(SELECT titles FROM titles t WHERE t.player_id = g.player_id AND t.level = 'F') AS tour_finals, " +
			"(SELECT titles FROM titles t WHERE t.player_id = g.player_id AND t.level = 'M') AS masters, " +
			"(SELECT titles FROM titles t WHERE t.player_id = g.player_id AND t.level = 'O') AS olympics, " +
			"(SELECT sum(titles) FROM titles t WHERE t.player_id = g.player_id) AS titles " +
		"FROM goat_list g";

	@RequestMapping("/goatTable")
	public BootgridTable<GOATListRow> goatTable(
		@RequestParam(value = "current") int current,
		@RequestParam(value = "rowCount") int rowCount
	) {
		BootgridTable<GOATListRow> table = new BootgridTable<>(current, PLAYER_COUNT);
		jdbcTemplate.query(
			GOAT_LIST_QUERY,
			(rs) -> {
				int goatRanking = rs.getInt("goat_ranking");
				String name = rs.getString("name");
				int goatPoints = rs.getInt("goat_points");
				GOATListRow row = new GOATListRow(goatRanking, name, goatPoints);
				row.setGrandSlams(rs.getInt("grand_slams"));
				row.setTourFinals(rs.getInt("tour_finals"));
				row.setMasters(rs.getInt("masters"));
				row.setOlympics(rs.getInt("olympics"));
				row.setTitles(rs.getInt("titles"));
				table.addRow(row);
			},
			rowCount, (current-1)*rowCount
		);
		return table;
	}
}
