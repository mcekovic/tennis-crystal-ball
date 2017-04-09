package org.strangeforest.tcb.stats.jobs;

import java.io.*;

import org.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

@Component
@Profile("openshift")
public class InProgressEventsJob {

	private static final Logger LOGGER = LoggerFactory.getLogger(InProgressEventsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.reload-in-progress-events}")
	public void reloadInProgressEvents() {
		try {
			LOGGER.info("Executing InProgressEventsJob...");
			Process process = new ProcessBuilder("../data-load/bin/data-load", "-i", "-Dtcb.db.connections=1").redirectErrorStream(true).start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				int exitCode = process.waitFor();
				StringBuilder sb = new StringBuilder(200);
				while (true) {
					String line = reader.readLine();
					if (line == null)
						break;
					sb.append(line).append('\n');
				}
				LOGGER.info(sb.toString());
				if (exitCode == 0)
					LOGGER.info("InProgressEventsJob finished.");
				else
					LOGGER.error("InProgressEventsJob exited with code {}.", exitCode);
			}
		}
		catch (Exception ex) {
			LOGGER.error("Error executing InProgressEventsJob.", ex);
		}
	}
}
