package com.asm.tavern.discord.discord.audio

import com.asm.tavern.discord.utilities.DiscordUtils
import com.asm.tavern.discord.utilities.stream.StreamChunkCollector
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.audio.AudioTrackInfo
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.discord.GuildId
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import javax.annotation.Nonnull

class QueueCommandHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(QueueCommandHandler.class)
	private final AudioService audioService

	QueueCommandHandler(AudioService audioService) {
		this.audioService = audioService
	}

	@Override
	Command getCommand() {
		TavernCommands.QUEUE
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		List<AudioTrackInfo> queue = audioService.getQueue(new GuildId(event.getGuild().getId()))
		logger.debug("The queue has ${queue.size()} tracks")
		if (!queue.isEmpty()) {
			int songIndex = 1
			queue.stream()
					.collect(StreamChunkCollector.take(10)).forEach({ tracks ->
				StringBuilder builder = new StringBuilder()
				tracks.forEach({ track -> builder.append("${songIndex++}. ")
						.append(track.title)
						.append(" - ")
						.append(DiscordUtils.escapeUrl(track.url.toString()))
						.append("\n")
				})
				event.getChannel().sendMessage(builder.toString()).queue()
			})
		}
		new CommandResultBuilder().success().build()
	}

}
