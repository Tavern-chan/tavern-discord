package com.tavern.discord.audio;

import com.google.common.collect.Iterables;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.audio.SongService;
import com.tavern.domain.model.audio.SpotifyService;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.GuildId;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class PlayModeCommandHandler implements CommandHandler {
    private final SongService songService;
    private final AudioService audioService;
    private final SpotifyService spotifyService;

    public PlayModeCommandHandler(SongService songService, AudioService audioService) {
        this.songService = songService;
        this.audioService = audioService;
        this.spotifyService = songService.getSpotifyService();
    }

    @Override
    public Command getCommand() {
        return TavernCommands.PLAY_MODE;
    }

    @Override
    public boolean supportsUsage(CommandArgumentUsage usage) {
        return true;
    }

    @Override
    public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
        if (!message.getArgs().isEmpty()) {
            final String categoryId = Iterables.getFirst(message.getArgs(), null);
            GuildId guildId = new GuildId(event.getGuild().getId());

            if (StreamSupport.stream(songService.getSongRegistry().getAll().spliterator(), false).anyMatch(s -> Objects.equals(s.getCategory(), categoryId))) {
                event.getChannel().sendMessage("Tavern will shuffle " + categoryId + " songs after the queue is empty.").queue();
                audioService.setCategory(categoryId);
                if (audioService.getNowPlaying(guildId) == null) {
                    audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager());
                    audioService.forcePlay(event.getChannel().asTextChannel());
                }
            } else {
                event.getChannel().sendMessage("No songs from the category: " + categoryId + " found.").queue();
            }
        } else {
            audioService.clearPlayMode();
            event.getChannel().sendMessage("Tavern play mode set to default.").queue();
        }

        return new CommandResultBuilder().success().build();
    }

}
