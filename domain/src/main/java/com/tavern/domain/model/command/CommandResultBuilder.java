package com.tavern.domain.model.command;

public class CommandResultBuilder {
	private boolean success;

	public CommandResultBuilder success() {
		success = true;
		return this;
	}

	public CommandResultBuilder error() {
		success = false;
		return this;
	}

	public CommandResult build() {
		return new CommandResult() {
			@Override
			public boolean success() {
				return success;
			}
		};
	}

}
