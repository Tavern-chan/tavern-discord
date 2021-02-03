package com.asm.tavern.discord.discord.audio

import com.asm.tavern.domain.model.DomainRegistry
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.Command
import com.asm.tavern.domain.model.command.CommandArgumentUsage
import com.asm.tavern.domain.model.command.CommandHandler
import com.asm.tavern.domain.model.command.CommandMessage
import com.asm.tavern.domain.model.command.CommandResult
import com.asm.tavern.domain.model.command.CommandResultBuilder
import com.asm.tavern.domain.model.discord.GuildId
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull

class SkipCommandHandler implements CommandHandler {

	@Override
	Command getCommand() {
		TavernCommands.SKIP
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		int skipAmount = message.args[0] ? Integer.parseInt(message.args[0]) : 1
		DomainRegistry.audioService().skip(new GuildId(event.getGuild().getId()), skipAmount)
		new CommandResultBuilder().success().build()
	}

}
