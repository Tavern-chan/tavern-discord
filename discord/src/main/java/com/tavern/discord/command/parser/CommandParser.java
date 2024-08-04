package com.tavern.discord.command.parser;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.tavern.domain.model.command.Command;
import com.tavern.domain.model.command.CommandArgumentUsage;
import com.tavern.domain.model.command.CommandMessage;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandParser {
	private final String prefix;
	private final Map<String, Command> commandMap;

	public CommandParser(String prefix, List<Command> commands) {
		this.prefix = prefix;
		this.commandMap = toCommandMap(commands);
	}

	public final String getPrefix() {
		return prefix;
	}

	public final Map<String, Command> getCommandMap() {
		return commandMap;
	}


	private Map<String, Command> toCommandMap(List<Command> commands) {
		return commands.stream()
			.collect(Collectors.toMap(command -> command.getName().toLowerCase(), Function.identity()));
	}

	/**
	 * Parse just the command without arguments or a usage
	 *
	 * @param message the message
	 * @return the command message without a usage or arguments
	 */
	public CommandMessage getCommandFromMessage(String message) {
		if (!message.trim().startsWith(prefix)) {
			return commandNotFoundResult(message);
		}

		// Create command token stack
		CommandTokenizer tokenizer = new CommandTokenizer(message);
		if (!tokenizer.hasNext()) {
			return commandNotFoundResult(message);
		}

		// Get command and sub-commands
		List<Command> commandList = locateCommand(commandMap, tokenizer);
		if (CollectionUtils.isEmpty(commandList)) {
			return commandNotFoundResult(message);
		}

		return new CommandMessage(commandList, null, null, message);
	}

	public CommandMessage parse(String message) {
		if (!message.trim().startsWith(prefix)) {
			return commandNotFoundResult(message);
		}

		// Create command token stack
		CommandTokenizer tokenizer = new CommandTokenizer(message);
		if (!tokenizer.hasNext()) {
			return commandNotFoundResult(message);
		}

		// Get command and sub-commands
		List<Command> commandList = locateCommand(commandMap, tokenizer);
		if (CollectionUtils.isEmpty(commandList)) {
			return commandNotFoundResult(message);
		}

		// Get usage and args
		List<String> args = Lists.reverse(tokenizer.popRemaining());
		Command command = Iterables.getLast(commandList);
		CommandArgumentUsage usage = command.getArgumentUsageRouter().route(command.getArgumentUsages(), args);
		if (null != usage) {
			return new CommandMessage(commandList, usage, args, message);
		}

		return commandNotFoundResult(message);
	}

	private CommandMessage commandNotFoundResult(String message) {
		return new CommandMessage(new ArrayList<>(), null, new ArrayList<>(), message);
	}

	private List<Command> locateCommand(Map<String, Command> commands, CommandTokenizer tokenizer) {
		List<Command> commandList = new ArrayList<>();
		Optional<Command> command = findCommand(tokenizer.pop(), prefix, commands);
		while (command.isPresent()) {
			commandList.add(command.get());
			if (tokenizer.hasNext()) {
				command = findCommand(tokenizer.peek(), "", toCommandMap(command.get().getSubCommands()));
				command.ifPresent(__ -> tokenizer.pop());
			} else {
				command = Optional.empty();
			}
		}

		return commandList;
	}

	private Optional<Command> findCommand(String token, String prefix, Map<String, Command> commands) {
		if (token.startsWith(prefix)) {
			token = token.replaceFirst(Pattern.quote(prefix), "");
		}

		return Optional.ofNullable(commands.get(token.toLowerCase()));
	}

}
