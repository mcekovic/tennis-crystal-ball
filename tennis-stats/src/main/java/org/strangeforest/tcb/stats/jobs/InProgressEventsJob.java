package org.strangeforest.tcb.stats.jobs;

import java.io.*;
import java.util.concurrent.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.service.*;

@Component
@Profile("openshift")
public class InProgressEventsJob {

	@Autowired private DataService dataService;

	private static final Logger LOGGER = LoggerFactory.getLogger(InProgressEventsJob.class);

	@Scheduled(cron = "${tennis-stats.jobs.reload-in-progress-events:0 0 * * * *}")
	public void reloadInProgressEvents() {
		try {
			LOGGER.info("Executing InProgressEventsJob...");
			Process process = new ProcessBuilder("../data-load/bin/data-load", "-ip", "-c 1").redirectErrorStream(true).start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				StringBuilder sb = new StringBuilder(200);
				sb.append("InProgressEventsJob output:");
				while (!process.waitFor(1l, TimeUnit.SECONDS))
					readOutput(reader, sb);
				int exitCode = process.waitFor();
				readOutput(reader, sb);
				LOGGER.info(sb.toString());
				if (exitCode == 0) {
					int cacheCount = dataService.clearCaches("InProgressEventForecast");
					LOGGER.info("InProgressEventsJob finished, {} cache(s) cleared.", cacheCount);
				}
				else
					LOGGER.error("InProgressEventsJob exited with code {}.", exitCode);
			}
		}
		catch (Exception ex) {
			LOGGER.error("Error executing InProgressEventsJob.", ex);
		}
	}

	private static void readOutput(BufferedReader reader, StringBuilder sb) throws IOException {
		while (true) {
			String line = reader.readLine();
			if (line == null)
				break;
			sb.append('\n').append(line);
		}
	}
}
