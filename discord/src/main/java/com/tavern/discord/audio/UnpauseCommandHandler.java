package com.tavern.discord.audio;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.GuildId;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class UnpauseCommandHandler implements CommandHandler {
	private final AudioService audioService;

	public UnpauseCommandHandler(AudioService audioService) {
		this.audioService = audioService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.UNPAUSE;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		audioService.unpause(new GuildId(event.getGuild().getId()));
		GuildId guildId = new GuildId(event.getGuild().getId());
		final String title = audioService.getNowPlaying(guildId).getInfo().getTitle();
		event.getChannel().sendMessage("Resuming: " + title).queue();
		return new CommandResultBuilder().success().build();
	}

}
