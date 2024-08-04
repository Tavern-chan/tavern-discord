package com.tavern.domain.model.command;

import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Possible arg combinations to use with a command
 * Ex:
 * $roll
 * $roll 6
 * $roll 1 6
 */
public class CommandArgumentUsage {
	private String name;
	private String description;
	private List<CommandArgument> args;
	private boolean varArgs;
	private String requiredRole;

	private CommandArgumentUsage(String name, String description, List<CommandArgument> args, boolean varArgs, String requiredRole) {
		this.name = name;
		this.description = description;
		this.args = args;
		this.varArgs = varArgs;
		this.requiredRole = requiredRole;
	}

	public boolean canUse(Member member) {
		return null == requiredRole
			|| member.getRoles().stream()
				.anyMatch(role -> Objects.equals(role.getName(), requiredRole));
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

	public final List<CommandArgument> getArgs() {
		return args;
	}

	public final boolean getVarArgs() {
		return varArgs;
	}

	public final boolean isVarArgs() {
		return varArgs;
	}

	public final String getRequiredRole() {
		return requiredRole;
	}

	public static class Builder {
		private final String name;
		private final String description;
		private List<CommandArgument> args = new ArrayList<CommandArgument>();
		private String requiredRole;
		private boolean varArgs;

		public Builder(String name, String description) {
			this.name = name;
			this.description = description;
		}

		public Builder add(CommandArgument argument) {
			this.args.add(argument);
			return this;
		}

		public Builder add(CommandArgument.Builder argument) {
			this.args.add(argument.build());
			return this;
		}

		public Builder varArgs() {
			this.varArgs = true;
			return this;
		}

		public Builder requireRole(String requiredRole) {
			this.requiredRole = requiredRole;
			return this;
		}

		public CommandArgumentUsage build() {
			return new CommandArgumentUsage(name, description, args, varArgs, requiredRole);
		}

		public final String getName() {
			return name;
		}

		public final String getDescription() {
			return description;
		}

		public List<CommandArgument> getArgs() {
			return args;
		}

		public void setArgs(List<CommandArgument> args) {
			this.args = args;
		}

		public String getRequiredRole() {
			return requiredRole;
		}

		public void setRequiredRole(String requiredRole) {
			this.requiredRole = requiredRole;
		}

		public boolean getVarArgs() {
			return varArgs;
		}

		public boolean isVarArgs() {
			return varArgs;
		}

		public void setVarArgs(boolean varArgs) {
			this.varArgs = varArgs;
		}

	}
}
