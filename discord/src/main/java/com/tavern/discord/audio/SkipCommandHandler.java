package com.tavern.discord.audio;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.GuildId;
import com.tavern.utilities.CollectionUtils;
import com.tavern.utilities.StringUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class SkipCommandHandler implements CommandHandler {
	private final AudioService audioService;

	public SkipCommandHandler(AudioService audioService) {
		this.audioService = audioService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.SKIP;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		final int skipAmount = CollectionUtils.first(message.getArgs())
			.filter(StringUtils::isNullOrBlank)
			.map(Integer::parseInt)
			.orElse(1);
		GuildId guildId = new GuildId(event.getGuild().getId());
		final String title = audioService.getNowPlaying(guildId) != null ? audioService.getNowPlaying(guildId).getInfo().getTitle() : "";
		if (!title.isEmpty() && skipAmount == 1) {
			event.getChannel().sendMessage("Skipping: " + title).queue();
		} else {
			event.getChannel().sendMessage("Skipping " + skipAmount + " songs").queue();
		}

		audioService.skip(guildId, skipAmount);
		return new CommandResultBuilder().success().build();
	}

}
