package com.asm.tavern.discord.discord.command.parser

import com.asm.tavern.discord.utilities.ObjectUtils
import com.asm.tavern.domain.model.command.Command
import com.asm.tavern.domain.model.command.CommandArgumentUsage
import com.asm.tavern.domain.model.command.CommandMessage

import java.util.regex.Pattern
import java.util.stream.Collectors

class CommandParser {
	final String prefix
	final Map<String, Command> commandMap

	CommandParser(String prefix, List<Command> commands) {
		this.prefix = prefix
		this.commandMap = toCommandMap(commands)
	}

	private Map<String, Command> toCommandMap(List<Command> commands) {
		commands.stream().collect(Collectors.toMap((Command command) -> command.name.toLowerCase(), ObjectUtils::identity))
	}

	/**
	 * Parse just the command without arguments or a usage
	 * @param message the message
	 * @return the command message without a usage or arguments
	 */
	CommandMessage getCommandFromMessage(String message) {
		if (!message.trim().startsWith(prefix)) {
			return commandNotFoundResult(message)
		}

		// Create command token stack
		CommandTokenizer tokenizer = new CommandTokenizer(message)
		if (!tokenizer.hasNext()) {
			return commandNotFoundResult(message)
		}

		// Get command and sub-commands
		List<Command> commandList = locateCommand(commandMap, tokenizer)
		if (!commandList) {
			return commandNotFoundResult(message)
		}

		new CommandMessage(commandList, null, null, message)
	}

	CommandMessage parse(String message) {
		if (!message.trim().startsWith(prefix)) {
			return commandNotFoundResult(message)
		}

		// Create command token stack
		CommandTokenizer tokenizer = new CommandTokenizer(message)
		if (!tokenizer.hasNext()) {
			return commandNotFoundResult(message)
		}

		// Get command and sub-commands
		List<Command> commandList = locateCommand(commandMap, tokenizer)
		if (!commandList) {
			return commandNotFoundResult(message)
		}

		// Get usage and args
		List<String> args = tokenizer.popRemaining().reverse()
		Command command = commandList.last()
		CommandArgumentUsage usage = command.argumentUsageRouter.route(command.argumentUsages, args)
		if (usage) {
			return new CommandMessage(commandList, usage, args, message)
		}

		return commandNotFoundResult(message)
	}

	private commandNotFoundResult(String message) {
		new CommandMessage([], null, [], message)
	}

	private List<Command> locateCommand(Map<String, Command> commands, CommandTokenizer tokenizer) {
		List<Command> commandList = []
		Optional<Command> command = findCommand(tokenizer.pop(), prefix, commands)
		while (command.isPresent()) {
			commandList.add(command.get())
			if (tokenizer.hasNext()) {
				command = findCommand(tokenizer.peek(), '', toCommandMap(command.get().getSubCommands()))
				command.ifPresent(_ -> tokenizer.pop())
			} else {
				command = Optional.empty()
			}
		}
		return commandList
	}

	private Optional<Command> findCommand(String token, String prefix, Map<String, Command> commands) {
		if (token.startsWith(prefix)) {
			token = token.replaceFirst(Pattern.quote(prefix), '')
		}
		Optional.ofNullable(commands.get(token.toLowerCase()))
	}

}
