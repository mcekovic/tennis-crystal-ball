package org.strangeforest.tcb.stats.jobs;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class DataLoadCommand {

	@Value("${tennis-stats.jobs.data-load.command:../data-load/bin/data-load}")
	private String dataLoadCommand;

	public int execute(String name, String... params) {
		int length = params.length;
		String[] command = new String[length + 1];
		command[0] = dataLoadCommand;
		System.arraycopy(params, 0, command, 1, length);
		return CommandExecutor.execute(name, command);
	}
}
