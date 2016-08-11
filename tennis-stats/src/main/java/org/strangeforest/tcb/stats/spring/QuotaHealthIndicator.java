package org.strangeforest.tcb.stats.spring;

import java.io.*;
import java.text.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.*;

import com.google.common.collect.*;

@Component
public class QuotaHealthIndicator implements HealthIndicator {

	@Value("${management.health.quota.block-threshold:10240}") private int blocksThreshold;
	@Value("${management.health.quota.files-threshold:100}") private int filesThreshold;

	private static final Logger LOGGER = LoggerFactory.getLogger(QuotaHealthIndicator.class);

	@Override public final Health health() {
		Health.Builder builder = new Health.Builder();
		try {
			Process process = new ProcessBuilder("/bin/sh", "-c", "quota | tail -n 1").redirectErrorStream(true).start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				if (process.waitFor() == 0)
					parseQuota(reader, builder);
				else
					parseError(reader, builder);
			}
		}
		catch (Exception ex) {
			LOGGER.error("Error getting quota.", ex);
			builder.withException(ex);
		}
		return builder.build();
	}

	private void parseQuota(BufferedReader reader, Health.Builder builder) throws IOException {
		String line = reader.readLine();
		if (line == null) {
			builder.withDetail("quota", "");
			return;
		}
		String[] items = line.trim().split("\\s+");
		if (items.length < 6) {
			builder.withDetail("quota", line);
			return;
		}
		long blocksUsed = Long.parseLong(items[0]);
		long blocksLimit = Long.parseLong(items[2]);
		long blocksFree = blocksLimit - blocksUsed;
		long filesUsed = Long.parseLong(items[3]);
		long filesLimit = Long.parseLong(items[5]);
		long filesFree = filesLimit - filesUsed;
		if (blocksFree >= blocksThreshold && filesFree >= filesThreshold)
			builder.up();
		else
			builder.outOfService();
		builder.withDetail("blocks", ImmutableMap.of(
			"limit", blocksLimit,
			"free", blocksFree,
			"pctFree", pct(blocksFree, blocksLimit),
			"threshold", blocksThreshold
		)).withDetail("files", ImmutableMap.of(
			"limit", filesLimit,
			"free", filesFree,
			"pctFree", pct(filesFree, filesLimit),
			"threshold", filesThreshold
		));
	}

	private static String pct(long free, long limit) {
		return new DecimalFormat("0.##%").format((double)free / limit);
	}

	private void parseError(BufferedReader reader, Health.Builder builder) throws IOException {
		for (int i = 1; true; i++) {
			String line = reader.readLine();
			if (line != null)
				builder.withDetail("message" + i, line);
			else
				break;
		}
	}
}