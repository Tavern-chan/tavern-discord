package com.asm.tavern.discord.discord.audio

import com.asm.tavern.domain.model.DomainRegistry
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.Command
import com.asm.tavern.domain.model.command.CommandArgumentUsage
import com.asm.tavern.domain.model.command.CommandHandler
import com.asm.tavern.domain.model.command.CommandMessage
import com.asm.tavern.domain.model.command.CommandResult
import com.asm.tavern.domain.model.command.CommandResultBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import javax.annotation.Nonnull

class PlayCommandHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(PlayCommandHandler.class)

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
		DomainRegistry.songService().songFromString(songId).ifPresentOrElse(song -> {
			DomainRegistry.audioService().join(event.getMember().getVoiceState(), event.getGuild().getAudioManager())
			DomainRegistry.audioService().play(event.getChannel(), song.uri)
		}, () -> event.getChannel().sendMessage("Unable to find song ${songId}"))
		new CommandResultBuilder().success().build()
	}

}
