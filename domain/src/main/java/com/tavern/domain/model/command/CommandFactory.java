package com.tavern.domain.model.command;

public class CommandFactory {
	public static Command.Builder command(String name, String description) {
		return Command.builder(name, description);
	}

	public static CommandArgument.Builder argument(String name, String description) {
		return CommandArgument.builder(name, description);
	}

	public static CommandArgumentUsage.Builder usage(String name, String description) {
		return CommandArgumentUsage.builder(name, description);
	}

}
