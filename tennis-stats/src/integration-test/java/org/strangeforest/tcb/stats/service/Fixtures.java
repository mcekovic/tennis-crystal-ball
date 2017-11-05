package org.strangeforest.tcb.stats.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.util.*;

@Service
public class Fixtures {
	
	@Autowired private PlayerService playerService;

	public int getPlayerId(String name) {
		return playerService.findPlayerId(name).orElseThrow(() -> new NotFoundException("player", name));
	}
}
