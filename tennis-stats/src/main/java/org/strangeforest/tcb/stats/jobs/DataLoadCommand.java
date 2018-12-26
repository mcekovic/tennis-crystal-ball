package org.strangeforest.tcb.stats.jobs;

import java.io.*;
import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import static java.util.Arrays.*;

@Component
public class DataLoadCommand {

	@Value("${tennis-stats.jobs.data-load.command:../data-load/bin/data-load}")
	private String dataLoadCommand;

	public static final int NOT_CHANGED = 80;

	private static final Logger LOGGER = LoggerFactory.getLogger(DataLoadCommand.class);

	public int execute(String name, String... params) {
		try {
			List<String> command = new ArrayList<>();
			command.add(dataLoadCommand);
			command.addAll(asList(params));
			LOGGER.info("Executing {} [{}]", name, String.join(" ", command));
			Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				StringBuilder sb = new StringBuilder(200);
				sb.append(name).append(" output:");
				while (!process.waitFor(1L, TimeUnit.SECONDS))
					readOutput(reader, sb);
				int exitCode = process.waitFor();
				readOutput(reader, sb);
				LOGGER.info(sb.toString());
				if (exitCode == 0)
					LOGGER.debug("{} finished.", name);
				else if (exitCode == NOT_CHANGED)
					LOGGER.debug("{} exited with code {}.", name, exitCode);
				else
					LOGGER.error("{} exited with code {}.", name, exitCode);
				return exitCode;
			}
		}
		catch (Exception ex) {
			LOGGER.error("Error executing {}.", name, ex);
			return -1;
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
