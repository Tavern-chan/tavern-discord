package com.asm.tavern.discord.discord.audio

import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.audio.SpotifyService
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.discord.GuildId
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

import javax.annotation.Nonnull
import java.util.stream.StreamSupport

class PlayModeCommandHandler implements CommandHandler{
    private final SongService songService
    private final AudioService audioService
    private final SpotifyService spotifyService

    PlayModeCommandHandler(SongService songService, AudioService audioService) {
        this.songService = songService
        this.audioService = audioService
        this.spotifyService = songService.getSpotifyService()
    }

    @Override
    Command getCommand() {
        TavernCommands.PLAY_MODE
    }

    @Override
    boolean supportsUsage(CommandArgumentUsage usage) {
        true
    }

    @Override
    CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
        if(!message.args.empty){
            String categoryId = message.args.first()
            GuildId guildId = new GuildId(event.guild.id)



            if(StreamSupport.stream(songService.getSongRegistry().getAll().spliterator(), false).anyMatch(s -> s.category == categoryId)) {
                event.getChannel().sendMessage("Tavern will shuffle ${categoryId} songs after the queue is empty.").queue()
                if(audioService.getNowPlaying(guildId) == null && audioService.getQueue(guildId).isEmpty()){
                    audioService
                }
                audioService.setCategory(categoryId)
            }
            else
                event.getChannel().sendMessage("No songs from the category: ${categoryId} found.").queue()
        }
        else{
            audioService.clearPlayMode()
            event.getChannel().sendMessage("Tavern play mode set to default.").queue()
        }


        new CommandResultBuilder().success().build()
    }

}
