package com.tavern.discord.audio;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class JoinCommandHandler implements CommandHandler {
	private final AudioService audioService;

	public JoinCommandHandler(AudioService audioService) {
		this.audioService = audioService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.JOIN;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager());
		return new CommandResultBuilder().success().build();
	}

}
