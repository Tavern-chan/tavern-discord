package com.tavern.discord.audio;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class LeaveCommandHandler implements CommandHandler {
	private final AudioService audioService;

	public LeaveCommandHandler(AudioService audioService) {
		this.audioService = audioService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.LEAVE;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		audioService.leave(event.getGuild());
		return new CommandResultBuilder().success().build();
	}

}
