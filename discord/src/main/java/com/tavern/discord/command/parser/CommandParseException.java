package com.tavern.discord.command.parser;

public class CommandParseException extends Exception {
	public CommandParseException(String message) {
		super(message);
	}

	public CommandParseException(Throwable t, String message) {
		super(message, t);
	}
}
