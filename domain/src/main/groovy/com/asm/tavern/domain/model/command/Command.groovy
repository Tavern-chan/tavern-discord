package com.asm.tavern.domain.model.command

class Command {
	final String name
	final String description
	final String tag
	final List<Command> subCommands
	final List<CommandArgumentUsage> argumentUsages
	final CommandArgumentUsageRouter argumentUsageRouter

	Command(String name, String description, String tag, List<Command> subCommands, List<CommandArgumentUsage> argumentUsages, CommandArgumentUsageRouter argumentUsageRouter) {
		this.name = name
		this.description = description
		this.tag = tag
		this.subCommands = subCommands
		this.argumentUsages = argumentUsages
		this.argumentUsageRouter = null == argumentUsageRouter ? new OrderedCommandArgumentUsageRouter() : argumentUsageRouter
	}

	static Builder builder(String name, String description) {
		new Builder(name, description)
	}

	static class Builder {
		final String name
		final String description
		String tag
		List<Command> subCommands = []
		List<CommandArgumentUsage> argumentUsages = []
		CommandArgumentUsageRouter argumentUsageRouter

		private Builder(String name, String description) {
			this.name = name
			this.description = description
		}

		Builder tag(String tag) {
			this.tag = tag
			this
		}

		Builder add(Command subCommand) {
			this.subCommands += subCommand
			this
		}

		Builder add(Builder subCommand) {
			this.subCommands += subCommand.build()
			this
		}

		Builder add(CommandArgumentUsage commandArgumentUsage) {
			this.argumentUsages += commandArgumentUsage
			this
		}

		Builder add(CommandArgumentUsage.Builder commandArgumentUsage) {
			this.argumentUsages += commandArgumentUsage.build()
			this
		}

		Builder usageRouter(CommandArgumentUsageRouter argumentUsageRouter) {
			this.argumentUsageRouter = argumentUsageRouter
			this
		}

		Command build() {
			new Command(name, description, tag, subCommands, argumentUsages, argumentUsageRouter)
		}
	}
}
