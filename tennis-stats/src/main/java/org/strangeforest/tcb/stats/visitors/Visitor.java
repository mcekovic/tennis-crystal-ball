package org.strangeforest.tcb.stats.visitors;

import java.time.*;

import com.google.common.base.*;

public class Visitor {

	private final long id;
	private final String ipAddress;
	private final String countryId;
	private final String country;
	private final String agentType;
	private int hits;
	private final Instant firstHit;
	private Instant lastHit;
	private transient boolean dirty;

	public Visitor(long id, String ipAddress, String countryId, String country, String agentType, int hits, Instant firstHit, Instant lastHit) {
		this.id = id;
		this.ipAddress = ipAddress;
		this.countryId = countryId;
		this.country = country;
		this.agentType = agentType;
		this.hits = hits;
		this.firstHit = firstHit;
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

	public String getAgentType() {
		return agentType;
	}

	public synchronized int getHits() {
		return hits;
	}

	public Instant getFirstHit() {
		return firstHit;
	}

	public synchronized Instant getLastHit() {
		return lastHit;
	}

	public synchronized Duration getAge() {
		return Duration.between(firstHit, lastHit);
	}

	public synchronized void visit() {
		lastHit = Instant.now();
		hits++;
		dirty = true;
	}

	public synchronized void unvisit() {
		hits--;
		dirty = true;
	}

	public boolean isMaxHitsBreached(Integer maxHits) {
		return maxHits != null && hits > maxHits;
	}

	public boolean isHitRateBreached(Double maxHitRate, Duration maxHitRateDelay) {
		if (maxHitRate == null)
			return false;
		Duration age = getAge();
		if (maxHitRateDelay != null && age.compareTo(maxHitRateDelay) <= 0)
			return false;
		return 1000.0 * hits / age.toMillis() > maxHitRate;
	}

	public synchronized boolean isDirty() {
		return dirty;
	}

	public synchronized void clearDirty() {
		dirty = false;
	}

	public synchronized boolean isExpired(Duration expiryTimeout) {
		return lastHit.plus(expiryTimeout).isBefore(Instant.now());
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("ipAddress", ipAddress)
			.add("countryId", countryId)
			.add("country", country)
			.add("agentType", agentType)
			.add("hits", hits)
			.add("firstHit", firstHit)
			.add("lastHit", lastHit)
			.add("dirty", dirty)
		.toString();
	}
}
