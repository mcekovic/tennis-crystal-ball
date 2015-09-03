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
		"SELECT player_id, name, country_id FROM player_v " +
		"WHERE name ILIKE '%' || ? || '%'" +
		"ORDER BY goat_points DESC NULLS LAST, best_rank DESC NULLS LAST LIMIT 20";

	@RequestMapping("/autocompletePlayer")
	public List<AutocompleteOption> autocompletePlayer(@RequestParam(value = "term") String term) {
		return jdbcTemplate.query(
			PLAYER_AUTOCOMPLETE_QUERY,
			(rs, rowNum) -> {
				String id = rs.getString("player_id");
				String name = rs.getString("name");
				String countryId = rs.getString("country_id");
				return new AutocompleteOption(id, name, name + " (" + countryId + ')');
			},
			term.trim().replace("\\s*", " ")
		);
	}
}
