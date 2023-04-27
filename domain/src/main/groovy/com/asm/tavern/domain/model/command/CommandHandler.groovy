package com.asm.tavern.domain.model.command

import com.asm.tavern.domain.model.discord.GuildId
import com.asm.tavern.domain.model.discord.UserId
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

import javax.annotation.Nonnull

/**
 * Handle a command invocation
 */
interface CommandHandler {
	/**
	 * Command handled by this handler
	 * @return
	 */
	Command getCommand()
	/**
	 * Check if this handler supports the given command usage
	 * @param usage the usage being used
	 * @return true if this handler supports the provided usage, false otherwise
	 */
	boolean supportsUsage(CommandArgumentUsage usage)

	/**
	 * Handle the invocation of the command
	 * @param event the discord event
	 * @param args arguments for the command invocation
	 * @return result of the command
	 */

	// Data needed:
	// TextChannel to output message
	// Guild to get tavern instance
	// UserId for finding their voice channel
	// CommandMessage of the command to be ran
	default CommandResult handle(TextChannel textChannel, @Nonnull Guild guild, Member member, CommandMessage message){
		new CommandResultBuilder().success().build()
	}


	/**
	 * Handle the invocation of the command
	 * @param event the discord event
	 * @param args arguments for the command invocation
	 * @return result of the command
	 */
	default CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message){
		new CommandResultBuilder().success().build()
	}

	/**
	 * Handle the invocation of the command
	 * @param event the discord event
	 * @param args arguments for the command invocation
	 * @return result of the command
	 */
	default CommandResult handle(@Nonnull ButtonInteractionEvent event, CommandMessage message){
		new CommandResultBuilder().success().build()
	}

	/**
	 * Handle the invocation of the command
	 * @param event the discord event
	 * @param args arguments for the command invocation
	 * @return result of the command
	 */
	default CommandResult handle(@Nonnull MessageChannelUnion messageChannelUnion, GuildId guildId, CommandMessage message){
		new CommandResultBuilder().success().build()
	}
}
