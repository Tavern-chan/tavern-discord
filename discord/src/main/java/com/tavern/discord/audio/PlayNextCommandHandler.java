package com.tavern.discord.audio;

import com.google.common.collect.Iterables;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.audio.SongService;
import com.tavern.domain.model.audio.SpotifyService;
import com.tavern.domain.model.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class PlayNextCommandHandler implements CommandHandler {
    private final SongService songService;
    private final AudioService audioService;
    private final SpotifyService spotifyService;

    public PlayNextCommandHandler(SongService songService, AudioService audioService) {
        this.songService = songService;
        this.audioService = audioService;
        this.spotifyService = songService.getSpotifyService();
    }

    @Override
    public Command getCommand() {
        return TavernCommands.PLAY_NEXT;
    }

    @Override
    public boolean supportsUsage(CommandArgumentUsage usage) {
        return true;
    }

    @Override
    public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
        final String songId = Iterables.getFirst(message.getArgs(), null);

        //Check for spotify link here so we can play multiple times for a playlist from spotify
        if (songId.contains(spotifyService.URI_CHECK_STRING)){
            spotifyService.getListOfSongsFromURL(songId).ifPresentOrElse( songList -> {
                audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager());
                songList.stream()
                    .forEach(song -> audioService.playNext(event.getChannel().asTextChannel(), song.getId().toString()));
            }, () -> {
                event.getChannel().sendMessage("Could not retrieve spotify songs from ${songId}").queue();
            });
        } else {
            songService.songFromString(songId).ifPresentOrElse(song -> {
                audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager());
                audioService.playNext(event.getChannel().asTextChannel(), song.getUri());
            }, () -> {
                // this will search the message on youtube
                audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager());
                audioService.playNext(event.getChannel().asTextChannel(), String.join(" ", message.getArgs()));
            });
        }
        return new CommandResultBuilder().success().build();
    }

}
