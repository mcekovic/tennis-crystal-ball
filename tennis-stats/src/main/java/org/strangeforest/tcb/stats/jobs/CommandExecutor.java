package org.strangeforest.tcb.stats.jobs;

import java.io.*;
import java.util.concurrent.*;

import org.slf4j.*;

public abstract class CommandExecutor {

	private static final int NOT_CHANGED = 80;

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

	public static int execute(String name, String... command) {
		try {
			LOGGER.info("Executing {} [{}]", name, String.join(" ", command));
			var process = new ProcessBuilder(command).redirectErrorStream(true).start();
			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				var sb = new StringBuilder(200);
				sb.append(name).append(" output:");
				while (!process.waitFor(1L, TimeUnit.SECONDS))
					readOutput(reader, sb);
				var exitCode = process.waitFor();
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
			var line = reader.readLine();
			if (line == null)
				break;
			sb.append('\n').append(line);
		}
	}
}
