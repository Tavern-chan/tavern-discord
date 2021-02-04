package com.asm.tavern.domain.model.command

import javax.annotation.Nullable

/**
 * Chooses the first usage in the provided order that has the correct number of arguments
 */
class OrderedCommandArgumentUsageRouter
		implements CommandArgumentUsageRouter {

	@Override
	@Nullable
	CommandArgumentUsage route(List<CommandArgumentUsage> usages, List<String> args) {
		for (CommandArgumentUsage usage: usages) {
			if (usage.args.size() == args.size() || usage.varArgs) {
				return usage
			}
		}
		null
	}

}
