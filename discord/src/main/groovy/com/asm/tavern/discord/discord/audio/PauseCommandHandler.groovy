package com.asm.tavern.discord.discord.audio


import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.discord.GuildId
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

import javax.annotation.Nonnull

class PauseCommandHandler implements CommandHandler {
	private final AudioService audioService

	PauseCommandHandler(AudioService audioService) {
		this.audioService = audioService
	}

	@Override
	Command getCommand() {
		TavernCommands.PAUSE
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		audioService.pause(new GuildId(event.getGuild().getId()))
		new CommandResultBuilder().success().build()
	}

	@Override
	CommandResult handle(@Nonnull SlashCommandInteractionEvent event, CommandMessage message) {
		audioService.pause(new GuildId(event.getGuild().getId()))
		new CommandResultBuilder().success().build()
	}

}
