package com.asm.tavern.discord.discord.audio

import com.asm.tavern.discord.utilities.DiscordUtils
import com.asm.tavern.discord.utilities.stream.StreamChunkCollector
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.command.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull
import java.util.stream.StreamSupport

class SongsCommandHandler implements CommandHandler {
	private final SongService songService

	SongsCommandHandler(SongService songService) {
		this.songService = songService
	}

	@Override
	Command getCommand() {
		TavernCommands.SONGS
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		StreamSupport.stream(songService.getSongRegistry().getAll().spliterator(), false)
				.collect(StreamChunkCollector.take(10))
				.forEach({ songs ->
					StringBuilder builder = new StringBuilder()
					songs.forEach({ song -> builder.append(song.id)
							.append(" - ")
							.append(DiscordUtils.escapeUrl(song.uri.toString()))
							.append("\n")
					})
					event.getChannel().sendMessage(builder.toString()).queue()
				})
		new CommandResultBuilder().success().build()
	}

}
