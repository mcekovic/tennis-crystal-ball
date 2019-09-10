package org.strangeforest.tcb.stats.util;

import java.time.*;
import java.util.concurrent.*;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.*;
import io.micrometer.core.instrument.simple.*;

import static org.assertj.core.api.Assertions.*;

class MicrometerTest {

	private MeterRegistry meterRegistry = new SimpleMeterRegistry();

	@Test
	void testTimer() {
		Timer timer = Timer.builder("TestTimer").publishPercentiles(0.10, 0.50, 0.90).register(meterRegistry);
		timer.record(Duration.ofMillis(0L));
		timer.record(Duration.ofMillis(50L));
		timer.record(Duration.ofMillis(100L));
		timer.record(Duration.ofMillis(150L));
		timer.record(Duration.ofMillis(200L));
		timer.record(Duration.ofMillis(250L));
		timer.record(Duration.ofMillis(300L));
		timer.record(Duration.ofMillis(350L));
		timer.record(Duration.ofMillis(400L));
		timer.record(Duration.ofMillis(450L));
//		timer.record(Duration.ofMillis(500L));

		System.out.println(timer);

		assertThat(timer.count()).isEqualTo(10L);
		assertThat(timer.max(TimeUnit.MILLISECONDS)).isEqualTo(450.0);
		assertThat(timer.mean(TimeUnit.MILLISECONDS)).isEqualTo(225.0, Offset.offset(1.0));
		HistogramSnapshot histogramSnapshot = timer.takeSnapshot();
		System.out.println(histogramSnapshot);
		System.out.println(timer.measure());
	}
}
