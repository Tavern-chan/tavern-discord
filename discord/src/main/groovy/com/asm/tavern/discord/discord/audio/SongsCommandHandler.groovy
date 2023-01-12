package com.asm.tavern.discord.discord.audio

import com.asm.tavern.discord.utilities.DiscordUtils
import com.asm.tavern.discord.utilities.stream.StreamChunkCollector
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.Song
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.command.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

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
	CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		StreamSupport.stream(songService.getSongRegistry().getAll().spliterator(), false)
				.sorted(Comparator.comparing((Song song) -> song.id.id))
				.collect(StreamChunkCollector.take(10))
				.forEach({ songs ->
						StringBuilder builder = new StringBuilder()
						songs.forEach({ song -> builder.append("[${song.id}](${DiscordUtils.escapeUrl(song.uri.toString())})\n")
					})
					event.getChannel().sendMessageEmbeds(new MessageEmbed(null, "Songs", builder.toString(), null, null, 0xFF0000, null, null, null, null, null,null, null)).queue()
				})
		new CommandResultBuilder().success().build()
	}

	@Override
	CommandResult handle(@Nonnull SlashCommandInteractionEvent event, CommandMessage message) {
		StreamSupport.stream(songService.getSongRegistry().getAll().spliterator(), false)
				.sorted(Comparator.comparing((Song song) -> song.id.id))
				.collect(StreamChunkCollector.take(10))
				.forEach({ songs ->
					StringBuilder builder = new StringBuilder()
					songs.forEach({ song -> builder.append("[${song.id}](${DiscordUtils.escapeUrl(song.uri.toString())})\n")
					})
					event.getChannel().sendMessageEmbeds(new MessageEmbed(null, "Songs", builder.toString(), null, null, 0xFF0000, null, null, null, null, null,null, null)).queue()
				})
		new CommandResultBuilder().success().build()
	}
}
