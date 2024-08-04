package com.tavern.discord.drinks;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.drinks.ComradeService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class UncomradeCommandHandler implements CommandHandler {
	private final ComradeService comradeService;

	public UncomradeCommandHandler(ComradeService comradeService) {
		this.comradeService = comradeService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.UNCOMRADE;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		comradeService.disable();
		event.getGuild().getSelfMember().modifyNickname(null).submit();
		return new CommandResultBuilder().success().build();
	}

}
