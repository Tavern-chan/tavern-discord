package com.asm.tavern.discord.discord.audio


import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.command.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull

class PlayCommandHandler implements CommandHandler {
	private final SongService songService
	private final AudioService audioService

	PlayCommandHandler(SongService songService, AudioService audioService) {
		this.songService = songService
		this.audioService = audioService
	}

	@Override
	Command getCommand() {
		TavernCommands.PLAY
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
			audioService.play(event.getChannel(), song.uri)
		}, () -> event.getChannel().sendMessage("Unable to find song ${songId}"))
		new CommandResultBuilder().success().build()
	}

}
