package com.tavern.discord.audio;

import com.tavern.discord.DiscordUtils;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.audio.AudioTrackInfo;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.GuildId;
import com.tavern.utilities.Ref;
import com.tavern.utilities.stream.StreamChunkCollector;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class QueueCommandHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(QueueCommandHandler.class);
	private final AudioService audioService;

	public QueueCommandHandler(AudioService audioService) {
		this.audioService = audioService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.QUEUE;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull final MessageReceivedEvent event, CommandMessage message) {
        GuildId guildId = new GuildId(event.getGuild().getId());
		final List<AudioTrackInfo> queue = audioService.getQueue(guildId);
		logger.debug("The queue has " + queue.size() + " tracks");

		if (!queue.isEmpty()) {
			final Ref<Integer> songIndex = new Ref<>(1);
			final Ref<Duration> totalDuration = new Ref<Duration>(Duration.ZERO);
			final int maxOutput = 20;
            final Duration nowPlayingTimeLeft = audioService.getNowPlaying(guildId).getInfo().getDuration().minus(audioService.getNowPlaying(guildId).getCurrentTime());

			final Function<Duration, String> formatTime = duration -> String.format("%d:%02d", Long.valueOf(duration.getSeconds()/60).intValue(), Long.valueOf(duration.getSeconds()%60).intValue());

			queue.stream().collect(StreamChunkCollector.take(10)).forEach(tracks -> {
				StringBuilder builder = new StringBuilder();
				tracks.forEach(track -> {
					if (songIndex.get() <= maxOutput) {
						try {
							builder.append(songIndex.get())
								.append(". ")
								.append(track.getTitle())
								.append(" - ")
								.append(DiscordUtils.escapeUrl(track.getUrl().toString()))
								.append("\n");
						} catch (Exception ex) {
							builder.append(songIndex.get())
								.append(". ")
								.append(track.getTitle())
								.append("\n");
						}
						songIndex.set(songIndex.get() + 1);
					}
					totalDuration.set(totalDuration.get().plus(track.getDuration()));
				});
				if (!builder.toString().isBlank()) {
					event.getChannel().sendMessage(builder.toString()).queue();
				}
			});
			if (queue.size() > maxOutput) {
				event.getChannel().sendMessage("+" + (queue.size() - maxOutput) + " more").queue();
			}
			event.getChannel().sendMessage("Time left in queue: " + formatTime.apply(totalDuration.get()) + " with " + formatTime.apply(nowPlayingTimeLeft) + " left in the now playing song").queue();
		}

		return new CommandResultBuilder().success().build();
	}

}
