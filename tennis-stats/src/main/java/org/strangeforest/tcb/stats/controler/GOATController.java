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
		"SELECT rank() OVER (ORDER BY goat_points DESC NULLS LAST) AS goat_ranking, name, goat_points FROM player_v " +
		"ORDER BY goat_points DESC NULLS LAST, name LIMIT ? OFFSET ?";

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
				table.addRow(new GOATListRow(goatRanking, name, goatPoints));
			},
			rowCount, (current-1)*rowCount
		);
		return table;
	}
}
