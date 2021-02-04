package com.asm.tavern.discord.discord.audio


import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.ActiveAudioTrack
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.discord.GuildId
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull
import java.time.Duration
import java.util.function.Function

class NowPlayingCommandHandler implements CommandHandler {
	private AudioService audioService

	NowPlayingCommandHandler(AudioService audioService) {
		this.audioService = audioService
	}

	@Override
	Command getCommand() {
		TavernCommands.NOW_PLAYING
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		ActiveAudioTrack track = audioService.getNowPlaying(new GuildId(event.getGuild().getId()))

		Function<Duration, String> formatTime = (Duration duration) -> {
			String.format("%d:%2d", (duration.getSeconds()/60).intValue(), (duration.getSeconds()%60).intValue()).replace(" ", "0")
		}

		if (track) {
			event.getChannel().sendMessage(new MessageEmbed(
					track.info.url.toString(),
					track.info.title,
					track.info.isStream() ? "Streaming" : "${formatTime(track.currentTime)}/${formatTime(track.info.duration)}",
					EmbedType.VIDEO,
					null,
					0,
					null,
					null,
					new MessageEmbed.AuthorInfo(track.info.author, null, null, null),
					new MessageEmbed.VideoInfo(track.info.url.toString(), 400, 300),
					null,
					null,
					null)).queue()
		}
		new CommandResultBuilder().success().build()
	}

}
