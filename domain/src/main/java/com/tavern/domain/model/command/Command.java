package com.tavern.domain.model.command;

import java.util.ArrayList;
import java.util.List;

public class Command {
	private final String name;
	private final String description;
	private final String tag;
	private final List<Command> subCommands;
	private final List<CommandArgumentUsage> argumentUsages;
	private final CommandArgumentUsageRouter argumentUsageRouter;

	public Command(String name, String description, String tag, List<Command> subCommands, List<CommandArgumentUsage> argumentUsages, CommandArgumentUsageRouter argumentUsageRouter) {
		this.name = name;
		this.description = description;
		this.tag = tag;
		this.subCommands = subCommands;
		this.argumentUsages = argumentUsages;
		this.argumentUsageRouter = null == argumentUsageRouter ? new OrderedCommandArgumentUsageRouter() : argumentUsageRouter;
	}

	public static Builder builder(String name, String description) {
		return new Builder(name, description);
	}

	public final String getName() {
		return name;
	}

	public final String getDescription() {
		return description;
	}

	public final String getTag() {
		return tag;
	}

	public final List<Command> getSubCommands() {
		return subCommands;
	}

	public final List<CommandArgumentUsage> getArgumentUsages() {
		return argumentUsages;
	}

	public final CommandArgumentUsageRouter getArgumentUsageRouter() {
		return argumentUsageRouter;
	}

	public static class Builder {
		private final String name;
		private final String description;
		private String tag;
		private List<Command> subCommands = new ArrayList<>();
		private List<CommandArgumentUsage> argumentUsages = new ArrayList<>();
		private CommandArgumentUsageRouter argumentUsageRouter;

		private Builder(String name, String description) {
			this.name = name;
			this.description = description;
		}

		public Builder tag(String tag) {
			this.tag = tag;
			return this;
		}

		public Builder add(Command subCommand) {
			this.subCommands.add(subCommand);
			return this;
		}

		public Builder add(Builder subCommand) {
			this.subCommands.add(subCommand.build());
			return this;
		}

		public Builder add(CommandArgumentUsage commandArgumentUsage) {
			this.argumentUsages.add(commandArgumentUsage);
			return this;
		}

		public Builder add(CommandArgumentUsage.Builder commandArgumentUsage) {
			this.argumentUsages.add(commandArgumentUsage.build());
			return this;
		}

		public Builder usageRouter(CommandArgumentUsageRouter argumentUsageRouter) {
			this.argumentUsageRouter = argumentUsageRouter;
			return this;
		}

		public Command build() {
			return new Command(name, description, tag, subCommands, argumentUsages, argumentUsageRouter);
		}

		public final String getName() {
			return name;
		}

		public final String getDescription() {
			return description;
		}

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

		public List<Command> getSubCommands() {
			return subCommands;
		}

		public void setSubCommands(List<Command> subCommands) {
			this.subCommands = subCommands;
		}

		public List<CommandArgumentUsage> getArgumentUsages() {
			return argumentUsages;
		}

		public void setArgumentUsages(List<CommandArgumentUsage> argumentUsages) {
			this.argumentUsages = argumentUsages;
		}

		public CommandArgumentUsageRouter getArgumentUsageRouter() {
			return argumentUsageRouter;
		}

		public void setArgumentUsageRouter(CommandArgumentUsageRouter argumentUsageRouter) {
			this.argumentUsageRouter = argumentUsageRouter;
		}

	}
}
