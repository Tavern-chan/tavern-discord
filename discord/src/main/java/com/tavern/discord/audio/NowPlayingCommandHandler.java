package com.tavern.discord.audio;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.ActiveAudioTrack;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.GuildId;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.function.Function;

public class NowPlayingCommandHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(NowPlayingCommandHandler.class);

	private AudioService audioService;

	public NowPlayingCommandHandler(AudioService audioService) {
		this.audioService = audioService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.NOW_PLAYING;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		final ActiveAudioTrack track = audioService.getNowPlaying(new GuildId(event.getGuild().getId()));

		final Function<Duration, String> formatTime = duration -> String.format(
			"%d:%02d",
			Long.valueOf(duration.getSeconds() / 60).intValue(),
			Long.valueOf(duration.getSeconds() % 60).intValue()
		);

		Function<String, String> getVideoImageID = videoUrl -> {
			try {
				String videoImageId = videoUrl.split("(?<=watch\\?v=)")[1];
				return String.format("https://img.youtube.com/vi/%s/sddefault.jpg", videoImageId);
			} catch (Exception ex) {
				logger.info("No VideoImage Found", ex);
				return null;
			}
		};

		EmbedBuilder eb = new EmbedBuilder();
		if ( null != track) {
			try {
				String videoImgUrl = getVideoImageID.apply(track.getInfo().getUrl().toString());
				eb.setTitle(track.getInfo().getTitle(), track.getInfo().getUrl().toString());// large hyperlink
				eb.setThumbnail(videoImgUrl);// Top right corner image
			} catch (Exception ex) {
				eb.setTitle(track.getInfo().getTitle());
				logger.info("Video Image was unable to be fetched: ", ex);
			}

            eb.setDescription("By: " + track.getInfo().getAuthor());
			eb.addField(
				"Duration:",
				String.format(
					"%s/%s",
					formatTime.apply(track.getCurrentTime()),
					formatTime.apply(track.getInfo().getDuration())
				),
				false
			);
			eb.setColor(0x5865F2); // blurple UwU

			event.getChannel().sendMessageEmbeds(eb.build())
				.setActionRow(
					Button.primary("skip", "Skip"),
					Button.primary("shuffle", "Shuffle"),
					Button.primary("pause", "Play/Pause")
				)
				.queue();
		}

		return new CommandResultBuilder().success().build();
	}

}
