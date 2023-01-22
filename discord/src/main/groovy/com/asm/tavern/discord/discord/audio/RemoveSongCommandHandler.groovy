package com.asm.tavern.discord.discord.audio

import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.SongId
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.command.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull

class RemoveSongCommandHandler implements CommandHandler {
	private final SongService songService

	RemoveSongCommandHandler(SongService songService) {
		this.songService = songService
	}

	@Override
	Command getCommand() {
		TavernCommands.SONGS
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		TavernCommands.SongsUsages.REMOVE == usage
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		String id = message.args[0]
		songService.remove(new SongId(id))
		event.getChannel().sendMessage("Removed song ${id}").queue()
		new CommandResultBuilder().success().build()
	}
}
