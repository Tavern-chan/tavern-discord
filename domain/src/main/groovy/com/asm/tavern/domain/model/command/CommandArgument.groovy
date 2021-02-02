package com.asm.tavern.domain.model.command

class CommandArgument {
	final String name
	final String description
	final String example

	CommandArgument(String name, String description, String example) {
		this.name = name
		this.description = description
		this.example = example
	}

	static Builder builder(String name, String description) {
		new Builder(name, description)
	}

	static class Builder {
		final String name
		final String description
		String example

		Builder(String name, String description) {
			this.name = name
			this.description = description
		}

		Builder example(String example) {
			this.example = example
			this
		}

		CommandArgument build() {
			new CommandArgument(name, description, example)
		}
	}
}
