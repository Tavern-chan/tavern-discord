package com.asm.tavern.domain.model.command

import groovy.transform.Immutable
import net.dv8tion.jda.api.entities.Member

/**
 * Possible arg combinations to use with a command
 * Ex:
 * $roll
 * $roll 6
 * $roll 1 6
 *
 */
@Immutable
class CommandArgumentUsage {
	String name
	String description
	List<CommandArgument> args
	boolean varArgs
	String requiredRole

	boolean canUse(Member member) {
		null == requiredRole || null != member.roles.find { role -> role.name == requiredRole }
	}

	static Builder builder(String name, String description) {
		new Builder(name, description)
	}

	static class Builder {
		final String name
		final String description
		List<CommandArgument> args = []
		String requiredRole
		boolean varArgs

		Builder(String name, String description) {
			this.name = name
			this.description = description
		}

		Builder add(CommandArgument argument) {
			this.args += argument
			this
		}

		Builder add(CommandArgument.Builder argument) {
			this.args += argument.build()
			this
		}

		Builder varArgs() {
			this.varArgs = true
			this
		}

		Builder requireRole(String requiredRole) {
			this.requiredRole = requiredRole
			this
		}

		CommandArgumentUsage build() {
			new CommandArgumentUsage(name, description, args, varArgs, requiredRole)
		}
	}
}
