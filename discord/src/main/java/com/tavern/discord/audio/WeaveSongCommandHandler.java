package com.tavern.discord.audio;

import com.google.common.collect.Iterables;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.audio.SongService;
import com.tavern.domain.model.audio.SpotifyService;
import com.tavern.domain.model.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class WeaveSongCommandHandler implements CommandHandler {
    private final SongService songService;
    private final AudioService audioService;
    private final SpotifyService spotifyService;

    public WeaveSongCommandHandler(SongService songService, AudioService audioService) {
        this.songService = songService;
        this.audioService = audioService;
        this.spotifyService = songService.getSpotifyService();
    }

    @Override
    public Command getCommand() {
        return TavernCommands.WEAVE;
    }

    @Override
    public boolean supportsUsage(CommandArgumentUsage usage) {
        return true;
    }

    @Override
    public CommandResult handle(@Nonnull MessageReceivedEvent event, final CommandMessage message) {
        if (message.getArgs().isEmpty()) {
            audioService.clearPlayMode();
            event.getChannel().sendMessage("Tavern will stop weaving after each song.").queue();
        } else {
            final String songId = Iterables.getFirst(message.getArgs(), null);
            //Check for spotify link here so we can play multiple times for a playlist from spotify
            if (songId.contains(spotifyService.URI_CHECK_STRING)) {
                spotifyService.getListOfSongsFromURL(songId).ifPresentOrElse(songlist -> {
                    audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager());
                    songlist.stream().findFirst().ifPresent(song -> audioService.setWeaveAudio(song.getId().toString()));
                }, () -> {
                    event.getChannel().sendMessage("Could not retrieve spotify songs from " + songId).queue();
                });
            } else {
                songService.songFromString(songId).ifPresentOrElse(song -> {
                    audioService.setWeaveAudio(song.getUri());
                    event.getChannel().sendMessage("Tavern will weave " + song.getId() + " after each song.").queue();;
                }, () -> {
                    audioService.setWeaveAudio(String.join(" ", message.getArgs()));
                    event.getChannel().sendMessage("Tavern will weave " + String.join(" ", message.getArgs()) + " after each song.").queue();
                });
            }
        }

        return new CommandResultBuilder().success().build();
    }

}
