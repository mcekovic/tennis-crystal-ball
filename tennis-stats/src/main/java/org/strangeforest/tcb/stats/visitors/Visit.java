package org.strangeforest.tcb.stats.visitors;

public final class Visit {

	public final Visitor visitor;
	public final String message;

	public Visit(Visitor visitor) {
		this(visitor, null);
	}

	public Visit(Visitor visitor, String message) {
		this.visitor = visitor;
		this.message = message;
	}

	public boolean isAllowed() {
		return message == null;
	}
}
