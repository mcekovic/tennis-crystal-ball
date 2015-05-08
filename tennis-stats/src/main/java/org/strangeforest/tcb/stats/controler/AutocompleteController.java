package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;

@RestController
public class AutocompleteController {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String PLAYER_AUTOCOMPLETE_QUERY =
		"SELECT player_id, first_name, last_name FROM player_v " +
		"WHERE first_name || ' ' || last_name ILIKE '%' || ? || '%'" +
		"ORDER BY best_rank, best_rank_points DESC LIMIT 20";

	@RequestMapping("/autocompletePlayer")
	public List<AutocompleteOption> autocompletePlayer(@RequestParam(value="term") String term) {
		return jdbcTemplate.query(
			PLAYER_AUTOCOMPLETE_QUERY,
			(rs, rowNum) -> {
				String id = rs.getString("player_id");
				String name = rs.getString("first_name") + ' ' + rs.getString("last_name");
				return new AutocompleteOption(id, name, name);
			},
			term.trim().replace("\\s*", " ")
		);
	}
}
