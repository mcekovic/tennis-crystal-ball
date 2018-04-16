package org.strangeforest.tcb.stats.spring;

import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;

import org.slf4j.*;
import org.springframework.stereotype.*;

import io.micrometer.core.instrument.*;

import static java.util.Arrays.*;

@Component
public class OSMemoryMetrics {

	private static final List<OSMemoryGauge> OS_MEMORY_GAUGES = asList(
		new OSMemoryGauge("os.memory.physical.total", "getTotalPhysicalMemorySize"),
		new OSMemoryGauge("os.memory.physical.free", "getFreePhysicalMemorySize"),
		new OSMemoryGauge("os.memory.virtual.committed", "getCommittedVirtualMemorySize"),
		new OSMemoryGauge("os.memory.swap.total", "getTotalSwapSpaceSize"),
		new OSMemoryGauge("os.memory.swap.free", "getFreeSwapSpaceSize")
	);

	private static final Logger LOGGER = LoggerFactory.getLogger(OSMemoryMetrics.class);

	public OSMemoryMetrics(MeterRegistry meterRegistry) {
		for (OSMemoryGauge gauge : OS_MEMORY_GAUGES)
			meterRegistry.gauge(gauge.name, gauge, OSMemoryGauge::getMemoryValue);
	}

	private static class OSMemoryGauge {

		private final String name;
		private final String methodName;

		OSMemoryGauge(String name, String methodName) {
			this.name = name;
			this.methodName = methodName;
		}

		double getMemoryValue() {
			return toKB(invoke(ManagementFactory.getOperatingSystemMXBean(), methodName));
		}

		private static Object invoke(Object obj, String methodName) {
			try {
				Method method = obj.getClass().getDeclaredMethod(methodName);
				method.setAccessible(true);
				return method.invoke(obj);
			}
			catch (IllegalAccessException | NoSuchMethodException ignored) {
				LOGGER.error(ignored.getMessage(), ignored);
			}
			catch (InvocationTargetException ex) {
				Throwable target = ex.getTargetException();
				if (target == null)
					target = ex;
				LOGGER.error(target.getMessage(), target);
			}
			catch (Exception ex) {
				LOGGER.error(ex.getMessage(), ex);
			}
			return 0;
		}

		private static double toKB(Object value) {
			return value instanceof Number ? ((Number)value).doubleValue() / 1024.0 : 0.0;
		}
	}
}
