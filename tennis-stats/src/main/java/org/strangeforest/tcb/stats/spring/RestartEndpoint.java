package org.strangeforest.tcb.stats.spring;

import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.*;

@Component @Endpoint(id = "restart")
@Profile("!dev")
public class RestartEndpoint {

	@WriteOperation
	public String restart() {
		TennisStatsApplication.restart();
		return "Restart initiated";
	}
}