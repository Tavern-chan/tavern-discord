package com.asm.tavern.domain.model.help

import com.asm.tavern.domain.model.command.Command
import com.google.common.collect.Streams

import java.util.stream.Collectors

class CommandIniPrinter {

	String print(String prefix, List<Command> commands) {
		Map<String, List<Command>> tagToCommandMap = commands.stream().collect(Collectors.toMap(Command::getTag, command -> [command], {c1, c2 -> Streams.concat(c1.stream(), c2.stream()).collect(Collectors.toList())}))
		StringBuilder message = new StringBuilder()

		message.append("```ini\n")
		tagToCommandMap.entrySet().stream()
				.sorted(Comparator.comparing({ entry -> entry.getKey() }))
				.forEach({entry ->
			def tag = entry.getKey()
			def value = entry.getValue()
			message.append("[${tag}]\n")
			value.forEach({command -> message.append("\t${command.name} - ${command.description}\n")})
			message.append("\n")
		})
		message.append("\nUse ${prefix}help <command> to get help with a command\n")
		message.append("```\n").toString()
	}

}
