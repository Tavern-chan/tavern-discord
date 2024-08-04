package com.tavern.discord.audio;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.GuildId;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class PauseCommandHandler implements CommandHandler {
	private final AudioService audioService;

	public PauseCommandHandler(AudioService audioService) {
		this.audioService = audioService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.PAUSE;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
        GuildId guildId = new GuildId(event.getGuild().getId());
        if (!audioService.getIsPaused(guildId)) {
            audioService.pause(guildId);
            final String title = audioService.getNowPlaying(guildId).getInfo().getTitle();
            event.getChannel().sendMessage("Pausing: " + title).queue();
        } else if (audioService.getIsPaused(guildId)) {
            audioService.unpause(guildId);
            final String title = audioService.getNowPlaying(guildId).getInfo().getTitle();
            event.getChannel().sendMessage("Resuming: " + title).queue();
        }

		return new CommandResultBuilder().success().build();
	}

}
