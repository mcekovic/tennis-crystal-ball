package org.strangeforest.tcb.stats.spring;

import java.io.*;

import org.slf4j.*;
import org.springframework.boot.actuate.health.*;

public abstract class OpenShiftHealthIndicator implements HealthIndicator {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenShiftHealthIndicator.class);

	@Override public final Health health() {
		Health.Builder builder = new Health.Builder();
		String command = getCommand();
		try {
			Process process = new ProcessBuilder("/bin/sh", "-c", command).redirectErrorStream(true).start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				if (process.waitFor() == 0)
					parseOutput(reader, builder);
				else
					parseError(reader, builder);
			}
		}
		catch (Exception ex) {
			LOGGER.error("Error executing health check command: ", command, ex);
			builder.withException(ex);
		}
		return builder.build();
	}

	protected abstract String getCommand();
	protected abstract void parseOutput(BufferedReader reader, Health.Builder builder) throws IOException;

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