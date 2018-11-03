package org.strangeforest.tcb.stats.spring;

import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;

import org.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import io.micrometer.core.instrument.*;

import static java.util.Arrays.*;

@Component
@Profile("!dev")
public class SystemMemoryMetrics {

	private static final List<SystemMemoryGauge> SYSTEM_MEMORY_GAUGES = asList(
		new SystemMemoryGauge("physical", "total", "getTotalPhysicalMemorySize"),
		new SystemMemoryGauge("physical", "free", "getFreePhysicalMemorySize"),
		new SystemMemoryGauge("virtual", "committed", "getCommittedVirtualMemorySize"),
		new SystemMemoryGauge("swap", "total", "getTotalSwapSpaceSize"),
		new SystemMemoryGauge("swap", "free", "getFreeSwapSpaceSize")
	);

	private static final String SYSTEM_MEMORY = "system.memory.mb";
	private static final String TYPE = "type";
	private static final String ID = "id";

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemMemoryMetrics.class);

	public SystemMemoryMetrics(MeterRegistry meterRegistry) {
		for (SystemMemoryGauge gauge : SYSTEM_MEMORY_GAUGES)
			meterRegistry.gauge(SYSTEM_MEMORY, asList(Tag.of(TYPE, gauge.type), Tag.of(ID, gauge.id)), gauge, SystemMemoryGauge::getMemoryValue);
	}

	private static class SystemMemoryGauge {

		private final String type;
		private final String id;
		private final String methodName;

		SystemMemoryGauge(String type, String id, String methodName) {
			this.type = type;
			this.id = id;
			this.methodName = methodName;
		}

		double getMemoryValue() {
			return toMB(invoke(ManagementFactory.getOperatingSystemMXBean(), methodName));
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

		private static double toMB(Object value) {
			return value instanceof Number ? ((Number)value).doubleValue() / 1048576.0 : 0.0;
		}
	}
}
