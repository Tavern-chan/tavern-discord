package com.tavern.domain.model.help;

import com.google.common.collect.Streams;
import com.tavern.domain.model.command.Command;

import java.util.*;
import java.util.stream.Collectors;

public class CommandIniPrinter {

	public String print(final String prefix, List<Command> commands) {
		Map<String, List<Command>> tagToCommandMap = commands.stream().collect(Collectors.toMap(
			Command::getTag,
			command -> new ArrayList<>(Collections.singleton(command)),
			(c1, c2) -> Streams.concat(c1.stream(), c2.stream()).collect(Collectors.toList())
		));

		StringBuilder message = new StringBuilder();
		message.append("```ini\n");
		tagToCommandMap.entrySet().stream()
			.sorted(Comparator.comparing(Map.Entry::getKey))
			.forEach(entry -> {
				String tag = entry.getKey();
				List<Command> value = entry.getValue();
				message.append("[").append(tag).append("]\n");
				value.forEach(command -> message.append("\t")
					.append(command.getName())
					.append(" - ")
					.append(command.getDescription())
					.append("\n")
				);
		});
		message.append("\nUse ").append(prefix).append("help <command> to get help with a command\n");
		return message.append("```\n").toString();
	}

}
