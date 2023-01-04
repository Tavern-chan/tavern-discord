package com.asm.tavern.discord.discord.audio

import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.command.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull

class PlayNextCommandHandler implements CommandHandler {
    private final AudioService audioService
    private final SongService songService

    PlayNextCommandHandler(SongService songService, AudioService audioService) {
        this.audioService = audioService
        this.songService = songService
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
    CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
        String songId = message.args.first()

        songService.songFromString(songId).ifPresentOrElse(song -> {
            audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager())
            audioService.playNext(event.getChannel(), song.uri)
        }, () -> event.getChannel().sendMessage("${songId} will be played next!"))
        new CommandResultBuilder().success().build()
    }
}