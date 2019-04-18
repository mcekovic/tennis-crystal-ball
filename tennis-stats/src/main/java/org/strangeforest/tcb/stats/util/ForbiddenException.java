package org.strangeforest.tcb.stats.util;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class ForbiddenException extends TennisStatsException {

	public ForbiddenException(String message) {
		super(message);
	}
}
