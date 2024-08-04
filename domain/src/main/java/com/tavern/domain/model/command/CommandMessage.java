package com.tavern.domain.model.command;

import java.util.List;
import java.util.stream.Collectors;

public class CommandMessage {
	private final List<Command> commandList;
	private final CommandArgumentUsage usage;
	private final List<String> args;
	private final String message;

	public CommandMessage(List<Command> commandList, CommandArgumentUsage usage, List<String> args, String message) {
		this.commandList = commandList;
		this.usage = usage;
		this.args = args;
		this.message = message;
	}

	public String getCommandString() {
		return commandList.stream().map(Command::getName).collect(Collectors.joining(" "));
	}

	public final List<Command> getCommandList() {
		return commandList;
	}

	public final CommandArgumentUsage getUsage() {
		return usage;
	}

	public final List<String> getArgs() {
		return args;
	}

	public final String getMessage() {
		return message;
	}

}
