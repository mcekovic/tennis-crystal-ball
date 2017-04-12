package org.strangeforest.tcb.stats.spring;

import java.io.*;
import java.util.*;
import java.util.function.Function;

import org.slf4j.*;
import org.springframework.boot.actuate.endpoint.*;
import org.springframework.boot.actuate.metrics.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import com.google.common.base.*;

import static java.util.Collections.*;

@Component
@Profile("openshift")
public class OpenShiftMetrics implements PublicMetrics {

	private static final String MEMORY_COMMAND =
		"oo-cgroup-read memory.usage_in_bytes; oo-cgroup-read memory.max_usage_in_bytes; oo-cgroup-read memory.limit_in_bytes; " +
		"oo-cgroup-read memory.memsw.usage_in_bytes; oo-cgroup-read memory.memsw.max_usage_in_bytes; oo-cgroup-read memory.memsw.limit_in_bytes";

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenShiftMetrics.class);

	@Override public Collection<Metric<?>> metrics() {
		return metricsFromCommand(MEMORY_COMMAND, this::parseMemory);
	}

	private Collection<Metric<?>> metricsFromCommand(String command, Function<BufferedReader, Collection<Metric<?>>> parser) {
		try {
			Process process = new ProcessBuilder("/bin/sh", "-c", command).redirectErrorStream(true).start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				int exitCode = process.waitFor();
				if (exitCode == 0)
					return parser.apply(reader);
				else
					LOGGER.error("Metrics command '{}' returned exit code {}", command, exitCode);
			}
		}
		catch (Exception ex) {
			LOGGER.error("Error executing metrics command: ", command, ex);
		}
		return emptyList();
	}

	private Collection<Metric<?>> parseMemory(BufferedReader reader) {
		try {
			Collection<Metric<?>> metrics = new ArrayList<>();
			addMemoryMetric(metrics, "usage", reader);
			addMemoryMetric(metrics, "maxUsage", reader);
			addMemoryMetric(metrics, "limit", reader);
			addMemoryMetric(metrics, "memsw.usage", reader);
			addMemoryMetric(metrics, "memsw.maxUsage", reader);
			addMemoryMetric(metrics, "memsw.limit", reader);
			return metrics;
		}
		catch (IOException ex) {
			LOGGER.error("Error reading metrics.", ex);
			return emptyList();
		}
	}
	
	private static void addMemoryMetric(Collection<Metric<?>> metrics, String name, BufferedReader reader) throws IOException {
		String bytes = reader.readLine();
		if (!Strings.isNullOrEmpty(bytes))
			metrics.add(new Metric<>("memory." + name, Integer.parseInt(bytes.trim()) / 1024));
	}
}
