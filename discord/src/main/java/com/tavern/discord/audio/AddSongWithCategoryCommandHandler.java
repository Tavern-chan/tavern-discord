package com.tavern.discord.audio;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.SongId;
import com.tavern.domain.model.audio.SongService;
import com.tavern.domain.model.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;

public class AddSongWithCategoryCommandHandler implements CommandHandler {
    private static final XLogger logger = XLoggerFactory.getXLogger(AddSongWithCategoryCommandHandler.class);

    private final SongService songService;

    public AddSongWithCategoryCommandHandler(SongService songService) {
        this.songService = songService;
    }

    @Override
    public Command getCommand() {
        return TavernCommands.SONGS;
    }

    @Override
    public boolean supportsUsage(CommandArgumentUsage usage) {
        return TavernCommands.SongsUsages.ADD_WITH_CATEGORY.equals(usage);
    }

    @Override
    public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
        final String id = message.getArgs().get(1);
        String uri = message.getArgs().get(2);
        final String category = message.getArgs().get(3);

        try {
            songService.register(new SongId(id), new URI(uri), category);
            event.getChannel().sendMessage("Registered new song " + id + " under category: " + category).queue();
            return new CommandResultBuilder().success().build();
        } catch (URISyntaxException ex) {
            logger.error("Failed to parse song URI", ex);
            return new CommandResultBuilder().error().build();
        }
    }
}
