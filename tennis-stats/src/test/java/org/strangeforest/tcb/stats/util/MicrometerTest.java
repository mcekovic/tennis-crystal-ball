package org.strangeforest.tcb.stats.util;

import java.time.*;
import java.util.concurrent.*;

import org.assertj.core.data.*;
import org.junit.jupiter.api.*;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.composite.*;
import io.micrometer.core.instrument.distribution.*;
import io.micrometer.prometheus.*;

import static org.assertj.core.api.Assertions.*;

class MicrometerTest {

	private MeterRegistry meterRegistry = createMeterRegistry();

	private static MeterRegistry createMeterRegistry() {
		CompositeMeterRegistry meterRegistry = new CompositeMeterRegistry();
		meterRegistry.add(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));
		return meterRegistry;
	}

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

	@RepeatedTest(100)
	void testTaggedCounterConcurrently() throws InterruptedException {
		int count = 10000;
		int tagCount = 100;
		var executor = Executors.newFixedThreadPool(10);
		for (int i = 0; i < count; i++) {
			int tagValue = i % tagCount;
			executor.execute(() ->
				createCounter(tagValue).increment()
			);
		}
		executor.shutdown();
		executor.awaitTermination(1L, TimeUnit.DAYS);
		for (int i = 0; i < tagCount; i++)
			assertThat(createCounter(i).count()).isEqualTo(count / tagCount);
	}

	@RepeatedTest(100)
	void testPreregisteredTaggedCounterConcurrently() throws InterruptedException {
		int count = 10000000;
		int tagCount = 10;
		var executor = Executors.newFixedThreadPool(10);
		var counters = new ConcurrentHashMap<Integer, Counter>();
		for (int i = 0; i < count; i++) {
			int tagValue = (i * 127) % tagCount;
			executor.execute(() ->
				counters.computeIfAbsent(tagValue, s -> createCounter(tagValue)).increment()
			);
		}
		executor.shutdown();
		executor.awaitTermination(1L, TimeUnit.DAYS);
		for (int i = 0; i < tagCount; i++)
			assertThat(createCounter(i).count()).isEqualTo(count / tagCount);
	}

	private Counter createCounter(int tagValue) {
		return Counter.builder("TestCounter").tag("test-tag", String.valueOf(tagValue)).register(meterRegistry);
	}
}
