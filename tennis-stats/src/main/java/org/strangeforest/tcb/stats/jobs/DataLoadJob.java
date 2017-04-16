package org.strangeforest.tcb.stats.jobs;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;

import static com.google.common.base.Strings.*;
import static java.util.stream.Collectors.*;

public abstract class DataLoadJob {

	private static final String DATA_LOAD_COMMAND = "../data-load/bin/data-load";

	private static final Logger LOGGER = LoggerFactory.getLogger(DataLoadJob.class);

	protected abstract Collection<String> params();
	protected abstract String onSuccess();

	protected void execute() {
		String jobName = getClass().getSimpleName();
		try {
			List<String> command = new ArrayList<>();
			command.add(DATA_LOAD_COMMAND);
			command.addAll(params());
			LOGGER.info("Executing {} [{}]", jobName, command.stream().collect(joining(" ")));
			Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				StringBuilder sb = new StringBuilder(200);
				sb.append(jobName).append(" output:");
				while (!process.waitFor(1L, TimeUnit.SECONDS))
					readOutput(reader, sb);
				int exitCode = process.waitFor();
				readOutput(reader, sb);
				LOGGER.info(sb.toString());
				if (exitCode == 0) {
					String message = onSuccess();
					LOGGER.info("{} finished" + (!isNullOrEmpty(message) ? ", " + message : "") + '.', jobName);
				}
				else
					LOGGER.error("{} exited with code {}.", jobName, exitCode);
			}
		}
		catch (Exception ex) {
			LOGGER.error("Error executing {}.", jobName, ex);
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
