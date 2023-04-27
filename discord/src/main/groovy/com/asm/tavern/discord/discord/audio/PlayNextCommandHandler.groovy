package com.asm.tavern.discord.discord.audio

import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.audio.SpotifyService
import com.asm.tavern.domain.model.command.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

import javax.annotation.Nonnull

class PlayNextCommandHandler implements CommandHandler {
    private final SongService songService
    private final AudioService audioService
    private final SpotifyService spotifyService

    PlayNextCommandHandler(SongService songService, AudioService audioService) {
        this.songService = songService
        this.audioService = audioService
        this.spotifyService = songService.getSpotifyService()
    }

    @Override
    Command getCommand() {
        TavernCommands.PLAY_NEXT
    }

    @Override
    boolean supportsUsage(CommandArgumentUsage usage) {
        true
    }

    @Override
    CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
        String songId = message.args.first()

        //Check for spotify link here so we can play multiple times for a playlist from spotify
        if(songId.contains(spotifyService.URI_CHECK_STRING)){
            spotifyService.getListOfSongsFromURL(songId).ifPresentOrElse( songList -> {
                audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager())
                songList.stream()
                        .forEach(song -> audioService.playNext(event.getChannel().asTextChannel(), song.id.toString()))

            }, () -> {
                event.getChannel().sendMessage("Could not retrieve spotify songs from ${songId}").queue()
            })
        }
        else{
            songService.songFromString(songId).ifPresentOrElse(song -> {
                audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager())
                audioService.playNext(event.getChannel().asTextChannel(), song.uri)
            }, () -> {
                // this will search the message on youtube
                audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager())
                audioService.playNext(event.getChannel().asTextChannel(), message.args.join(" "))
            })
        }
        new CommandResultBuilder().success().build()
    }
}