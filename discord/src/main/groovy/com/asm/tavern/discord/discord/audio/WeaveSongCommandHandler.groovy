package com.asm.tavern.discord.discord.audio

import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.audio.SpotifyService
import com.asm.tavern.domain.model.command.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

import javax.annotation.Nonnull

class WeaveSongCommandHandler implements CommandHandler{
    private final SongService songService
    private final AudioService audioService
    private final SpotifyService spotifyService

    WeaveSongCommandHandler(SongService songService, AudioService audioService) {
        this.songService = songService
        this.audioService = audioService
        this.spotifyService = songService.getSpotifyService()
    }

    @Override
    Command getCommand() {
        TavernCommands.WEAVE
    }

    @Override
    boolean supportsUsage(CommandArgumentUsage usage) {
        true
    }

    @Override
    CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
        if(!message.args.empty){
            String songId = message.args.first()
            //Check for spotify link here so we can play multiple times for a playlist from spotify
            if(songId.contains(spotifyService.URI_CHECK_STRING)){
                spotifyService.getListOfSongsFromURL(songId).ifPresentOrElse( songList -> {
                    audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager())
                    songList.stream().findFirst().ifPresent(song -> audioService.setWeaveAudio(song.id.toString()))

                }, () -> {
                    event.getChannel().sendMessage("Could not retrieve spotify songs from ${songId}").queue()
                })
            }
            else{
                songService.songFromString(songId).ifPresentOrElse(song -> {
                    audioService.setWeaveAudio(song.uri)
                    event.getChannel().sendMessage("Tavern will weave ${song.id} after each song.").queue()
                }, () -> {
                    // this will search the message on youtube
                    audioService.setWeaveAudio(message.args.join(" "))
                    event.getChannel().sendMessage("Tavern will weave ${message.args.join(" ")} after each song.").queue()
                })
            }
        }
        else{
            audioService.clearPlayMode()
            event.getChannel().sendMessage("Tavern will stop weaving after each song.").queue()
        }

        new CommandResultBuilder().success().build()
    }

}
