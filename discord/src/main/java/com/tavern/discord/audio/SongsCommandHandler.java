package com.tavern.discord.audio;

import com.tavern.discord.DiscordUtils;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.Song;
import com.tavern.domain.model.audio.SongService;
import com.tavern.domain.model.command.*;
import com.tavern.utilities.Ref;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SongsCommandHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(SongsCommandHandler.class);

	private final SongService songService;

	public SongsCommandHandler(SongService songService) {
		this.songService = songService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.SONGS;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return TavernCommands.SongsUsages.DEFAULT.equals(usage);
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		final int chunkSize = 10;
		final Ref<String> currentCategory = new Ref<>("");

		// Function for pushing message so message can be pushed when max chunk size to avoid discord max message length. Also empty builder when pushed
		final Consumer<StringBuilder> pushMessage = builder -> {
			event.getChannel().asTextChannel().sendMessageEmbeds(new MessageEmbed(
				null,
				currentCategory.get() + " Songs",
				builder.toString(),
				null,
				null,
				0xFF0000,
				null,
				null,
				null,
				null,
				null,
				null,
				null
			)).queue();
			builder.setLength(0);
		};

		StreamSupport.stream(songService.getSongRegistry().getAll().spliterator(), false)
			.sorted(Comparator.comparing(song -> song.getId().getId()))
			.collect(Collectors.groupingBy(Song::getCategory))
			.values()
			.forEach(songs -> {
				int count = 0;
				StringBuilder builder = new StringBuilder();
				for (Song song: songs) {
					currentCategory.set(song.getCategory());
					try {
						new URL(song.getUri().toString());
						builder.append("[")
							.append(song.getId())
							.append("](")
							.append(DiscordUtils.escapeUrl(song.getUri().toString()))
							.append(")\n");
					} catch (Exception ex) {
						logger.info("File was most likely local: ", ex);
						builder.append(song.getId())
							.append("\n");
					}

					if (++count == chunkSize) {
						pushMessage.accept(builder);
					}
				}
				pushMessage.accept(builder);
			});

		return new CommandResultBuilder().success().build();
	}

}
