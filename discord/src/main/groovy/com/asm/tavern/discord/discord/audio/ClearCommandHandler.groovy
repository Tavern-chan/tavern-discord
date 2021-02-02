package com.asm.tavern.discord.discord.audio

import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.discord.GuildId
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull

class ClearCommandHandler implements CommandHandler {
	private final AudioService audioService

	ClearCommandHandler(AudioService audioService) {
		this.audioService = audioService
	}

	@Override
	Command getCommand() {
		TavernCommands.CLEAR
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		audioService.clear(new GuildId(event.getGuild().getId()))
		event.getChannel().sendMessage("Cleared song queue").queue()
		new CommandResultBuilder().success().build()
	}

}
