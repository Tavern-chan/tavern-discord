package com.tavern.discord.audio;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.SongId;
import com.tavern.domain.model.audio.SongService;
import com.tavern.domain.model.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class RemoveSongCommandHandler implements CommandHandler {
	private final SongService songService;

	public RemoveSongCommandHandler(SongService songService) {
		this.songService = songService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.SONGS;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return TavernCommands.SongsUsages.REMOVE.equals(usage);
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		final String id = message.getArgs().get(1);
		songService.remove(new SongId(id));
		event.getChannel().sendMessage("Removed song " + id).queue();
		return new CommandResultBuilder().success().build();
	}
}
