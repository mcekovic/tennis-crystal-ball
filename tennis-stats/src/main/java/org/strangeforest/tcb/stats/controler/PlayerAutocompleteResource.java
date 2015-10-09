package org.strangeforest.tcb.stats.controler;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.service.*;

@RestController
public class PlayerAutocompleteResource {

	@Autowired private PlayerService playerService;

	@RequestMapping("/autocompletePlayer")
	public List<AutocompleteOption> autocompletePlayer(
		@RequestParam(value = "term") String term
	) {
		return playerService.autocompletePlayer(term.trim().replace("\\s*", " "));
	}
}
