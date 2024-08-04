package com.tavern.domain.model.command;

public class CommandArgument {
	private final String name;
	private final String description;
	private final String example;

	public CommandArgument(String name, String description, String example) {
		this.name = name;
		this.description = description;
		this.example = example;
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

	public final String getExample() {
		return example;
	}

	public static class Builder {
		private final String name;
		private final String description;
		private String example;

		public Builder(String name, String description) {
			this.name = name;
			this.description = description;
		}

		public Builder example(String example) {
			this.example = example;
			return this;
		}

		public CommandArgument build() {
			return new CommandArgument(name, description, example);
		}

		public final String getName() {
			return name;
		}

		public final String getDescription() {
			return description;
		}

		public String getExample() {
			return example;
		}

		public void setExample(String example) {
			this.example = example;
		}

	}
}
