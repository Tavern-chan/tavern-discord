package com.tavern.domain.model.command;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Chooses the first usage in the provided order that has the correct number of arguments
 */
public class OrderedCommandArgumentUsageRouter implements CommandArgumentUsageRouter {

	@Override
	@Nullable
	public CommandArgumentUsage route(List<CommandArgumentUsage> usages, List<String> args) {
		for (CommandArgumentUsage usage : usages) {
			if (usage.getArgs().size() == args.size() || usage.getVarArgs()) {
				return usage;
			}
		}

		return null;
	}

}
