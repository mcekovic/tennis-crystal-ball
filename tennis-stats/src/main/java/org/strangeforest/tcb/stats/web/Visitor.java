package org.strangeforest.tcb.stats.web;

import java.time.*;

public class Visitor {

	private final long id;
	private final String ipAddress;
	private final String countryId;
	private int visits;
	private Instant lastVisit;
	private boolean dirty;

	public Visitor(long id, String ipAddress, String countryId, int visits, Instant lastVisit) {
		this.id = id;
		this.ipAddress = ipAddress;
		this.countryId = countryId;
		this.visits = visits;
		this.lastVisit = lastVisit;
	}

	public long getId() {
		return id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getCountryId() {
		return countryId;
	}

	public synchronized int getVisits() {
		return visits;
	}

	public synchronized Instant getLastVisit() {
		return lastVisit;
	}

	public synchronized int visit() {
		lastVisit = Instant.now();
		return ++visits;
	}

	public synchronized boolean isDirty() {
		return dirty;
	}

	public synchronized void setDirty() {
		dirty = true;
	}

	public synchronized void clearDirty() {
		dirty = false;
	}

	public synchronized boolean isExpired(Duration expiryTimeout) {
		return lastVisit.plus(expiryTimeout).isBefore(Instant.now());
	}
}
