package org.strangeforest.tcb.stats.spring;

import java.io.*;

import org.springframework.boot.actuate.health.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

@Component
@Profile("openshift")
public class MemoryHealthIndicator extends OpenShiftHealthIndicator {

	@Override protected String getCommand() {
		return "oo-cgroup-read memory.usage_in_bytes; oo-cgroup-read memory.max_usage_in_bytes; oo-cgroup-read memory.limit_in_bytes; " +
			    "oo-cgroup-read memory.memsw.usage_in_bytes; oo-cgroup-read memory.memsw.max_usage_in_bytes; oo-cgroup-read memory.memsw.limit_in_bytes";
	}

	@Override protected void parseOutput(BufferedReader reader, Health.Builder builder) throws IOException {
		if (!readMemoryDetail(reader, builder, "usageInBytes")) return;
		if (!readMemoryDetail(reader, builder, "maxUsageInBytes")) return;
		if (!readMemoryDetail(reader, builder, "limitInBytes")) return;
		if (!readMemoryDetail(reader, builder, "memsw.usageInBytes")) return;
		if (!readMemoryDetail(reader, builder, "memsw.maxUsageInBytes")) return;
		if (!readMemoryDetail(reader, builder, "memsw.limitInBytes")) return;
		builder.up();
	}

	private static boolean readMemoryDetail(BufferedReader reader, Health.Builder builder, String name) throws IOException {
		String usageInBytes = reader.readLine();
		if (usageInBytes != null) {
			builder.withDetail(name, usageInBytes);
			return true;
		}
		else
			return false;
	}
}