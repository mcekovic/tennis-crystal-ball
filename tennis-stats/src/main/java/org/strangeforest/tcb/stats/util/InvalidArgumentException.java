package org.strangeforest.tcb.stats.util;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidArgumentException extends TennisStatsException {

	public InvalidArgumentException(String message) {
		super(message);
	}
}
