package org.strangeforest.tcb.stats.spring;

import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;

import org.slf4j.*;
import org.springframework.boot.actuate.endpoint.*;
import org.springframework.boot.actuate.metrics.*;
import org.springframework.stereotype.*;

import static java.util.Arrays.*;

@Component
public class OSMemoryMetrics implements PublicMetrics {

	private static final Logger LOGGER = LoggerFactory.getLogger(OSMemoryMetrics.class);

	@Override public Collection<Metric<?>> metrics() {
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		return asList(
			new Metric("os.memory.physical.total", toKB(invoke(os, "getTotalPhysicalMemorySize"))),
			new Metric("os.memory.physical.free", toKB(invoke(os, "getFreePhysicalMemorySize"))),
			new Metric("os.memory.virtual.committed", toKB(invoke(os, "getCommittedVirtualMemorySize"))),
			new Metric("os.memory.swap.total", toKB(invoke(os, "getTotalSwapSpaceSize"))),
			new Metric("os.memory.swap.free", toKB(invoke(os, "getFreeSwapSpaceSize")))
		);
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

	private static long toKB(Object value) {
		return value instanceof Number ? ((Number)value).longValue() / 1024 : 0L;
	}
}
