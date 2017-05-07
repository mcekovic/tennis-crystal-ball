package org.strangeforest.tcb.stats.util;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import static java.lang.String.*;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NotFoundException extends TennisStatsException {

	public NotFoundException(String name, Object id) {
		super(format("%1$s %2$s not found.", name, id));
	}
}
