package com.tavern.domain.model.command;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

/**
 * Handle a command invocation
 */
public interface CommandHandler {
	/**
	 * Command handled by this handler
	 *
	 * @return
	 */
	Command getCommand();

	/**
	 * Check if this handler supports the given command usage
	 *
	 * @param usage the usage being used
	 * @return true if this handler supports the provided usage, false otherwise
	 */
	boolean supportsUsage(CommandArgumentUsage usage);

	/**
	 * Handle the invocation of the command
	 *
	 * @param event the discord event
	 * @param message the command message
	 * @return result of the command
	 */
	CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message);

	/**
	 * Handle the invocation of the command
	 *
	 * @param event the discord event
	 * @param message the command message
	 * @return result of the command
	 */
	default CommandResult handle(@Nonnull ButtonInteractionEvent event, CommandMessage message) {
		return new CommandResultBuilder().success().build();
	}

}
