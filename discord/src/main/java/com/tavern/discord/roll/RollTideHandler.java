package com.tavern.discord.roll;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class RollTideHandler implements CommandHandler {
	@Override
	public Command getCommand() {
		return TavernCommands.ROLL;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return TavernCommands.RollUsages.ROLL_TIDE.equals(usage);
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		event.getChannel().sendMessage("What are you doing step bro!?").queue();
		return new CommandResultBuilder().success().build();
	}
}
