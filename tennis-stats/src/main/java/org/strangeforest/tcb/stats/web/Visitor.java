package org.strangeforest.tcb.stats.web;

import java.time.*;

public class Visitor {

	private final long id;
	private final String ipAddress;
	private final String countryId;
	private final String country;
	private int hits;
	private Instant lastHit;
	private boolean dirty;

	public Visitor(long id, String ipAddress, String countryId, String country, int hits, Instant lastHit) {
		this.id = id;
		this.ipAddress = ipAddress;
		this.countryId = countryId;
		this.country = country;
		this.hits = hits;
		this.lastHit = lastHit;
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

	public String getCountry() {
		return country;
	}

	public synchronized int getHits() {
		return hits;
	}

	public synchronized boolean isFirstHit() {
		return hits == 1;
	}

	public synchronized Instant getLastHit() {
		return lastHit;
	}

	public synchronized int visit() {
		lastHit = Instant.now();
		return ++hits;
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
		return lastHit.plus(expiryTimeout).isBefore(Instant.now());
	}
}
