package org.strangeforest.tcb.stats.visitors;

import java.time.*;

import org.junit.jupiter.api.*;

import static eu.bitwalker.useragentutils.BrowserType.*;
import static org.assertj.core.api.Assertions.*;

class VisitorTest {

	@Test
	void checkIfMaxHitsIsBreached() {
		Instant now = Instant.now();
		Visitor visitor = new Visitor(1L, "192.168.1.1", "SRB", "Serbia", WEB_BROWSER.name(), 100, now, now);

		assertThat(visitor.isMaxHitsBreached(null)).isFalse();
		assertThat(visitor.isMaxHitsBreached(1000)).isFalse();
		assertThat(visitor.isMaxHitsBreached(10)).isTrue();
	}

	@Test
	void checkIfMaxHitRateIsBreached() {
		Instant now = Instant.now();
		Visitor visitor = new Visitor(1L, "192.168.1.1", "SRB", "Serbia", WEB_BROWSER.name(), 200, now, now.plus(Duration.ofMinutes(2)));

		assertThat(visitor.isHitRateBreached(null, null)).isFalse();
		assertThat(visitor.isHitRateBreached(1.0, null)).isTrue();
		assertThat(visitor.isHitRateBreached(1.0, Duration.ofMinutes(5))).isFalse();
		assertThat(visitor.isHitRateBreached(1.0, Duration.ofSeconds(1))).isTrue();
		assertThat(visitor.isHitRateBreached(10.0, null)).isFalse();
		assertThat(visitor.isHitRateBreached(10.0, Duration.ofSeconds(1))).isFalse();
	}
}
