package org.strangeforest.tcb.stats.spring;

import java.io.*;
import java.text.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.health.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import com.google.common.collect.*;

@Component
@Profile("openshift")
public class QuotaHealthIndicator extends OpenShiftHealthIndicator {

	@Value("${management.health.quota.block-threshold:10240}") private int blocksThreshold;
	@Value("${management.health.quota.files-threshold:100}") private int filesThreshold;

	@Override protected String getCommand() {
		return "quota | tail -n 1";
	}

	@Override protected void parseOutput(BufferedReader reader, Health.Builder builder) throws IOException {
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
			"threshold", blocksThreshold,
			"pctUsed", pct(blocksUsed, blocksLimit)
		)).withDetail("files", ImmutableMap.of(
			"limit", filesLimit,
			"free", filesFree,
			"threshold", filesThreshold,
			"pctUsed", pct(filesUsed, filesLimit)
		));
	}

	private static String pct(long part, long total) {
		return new DecimalFormat("0.##%").format((double)part / total);
	}
}