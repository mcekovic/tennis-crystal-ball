package org.strangeforest.tcb.stats.service;

import static java.lang.String.*;

public class NotFoundException extends RuntimeException {

	public NotFoundException(String name, Object id) {
		super(format("%1$s %2$s not found.", name, id));
	}

	@Override public synchronized Throwable fillInStackTrace() {
		return this;
	}
}
