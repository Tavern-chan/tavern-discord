package com.tavern.domain.model.command;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Strategy for choosing the command argument usage to use
 */
public interface CommandArgumentUsageRouter {
	/**
	 * Choose the usage to use given the provided list of usages and arguments.
	 * If none, return null
	 *
	 * @param usages the available list of usages
	 * @param args   the list of arguments
	 * @return the usage to use or null if none
	 */
	@Nullable
	CommandArgumentUsage route(List<CommandArgumentUsage> usages, List<String> args);
}
