package com.asm.tavern.discord.discord.audio


import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.discord.GuildId
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

import javax.annotation.Nonnull

class SkipCommandHandler implements CommandHandler {
	private final AudioService audioService

	SkipCommandHandler(AudioService audioService) {
		this.audioService = audioService
	}

	@Override
	Command getCommand() {
		TavernCommands.SKIP
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		int skipAmount = message.args[0] ? Integer.parseInt(message.args[0]) : 1
		GuildId guildId = new GuildId(event.getGuild().getId())
		String title = audioService.getNowPlaying(guildId) != null ? audioService.getNowPlaying(guildId).info.title : ""
		if(!title.empty && skipAmount == 1){
			event.getChannel().sendMessage("Skipping: ${title}").queue()
		}
		else if(!title.empty){
			event.getChannel().sendMessage("Skipping ${skipAmount} songs").queue()
		}

		audioService.skip(guildId, skipAmount)
		new CommandResultBuilder().success().build()
	}

}
