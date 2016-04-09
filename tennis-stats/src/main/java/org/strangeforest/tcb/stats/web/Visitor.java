package org.strangeforest.tcb.stats.web;

import java.time.*;

public class Visitor {

	private final long id;
	private final String ipAddress;
	private int visits;
	private Instant lastVisit;

	public Visitor(long id, String ipAddress, int visits, Instant lastVisit) {
		this.id = id;
		this.ipAddress = ipAddress;
		this.visits = visits;
		this.lastVisit = lastVisit;
	}

	public long getId() {
		return id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getVisits() {
		return visits;
	}

	public Instant getLastVisit() {
		return lastVisit;
	}

	public int visit() {
		lastVisit = Instant.now();
		return ++visits;
	}

	public boolean isExpired(Duration expiryTimeout) {
		return lastVisit.plus(expiryTimeout).isBefore(Instant.now());
	}
}
