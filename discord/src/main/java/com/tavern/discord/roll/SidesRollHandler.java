package com.tavern.discord.roll;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.roll.RollService;
import com.tavern.utilities.CollectionUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class SidesRollHandler implements CommandHandler {
	private final RollService rollService;

	public SidesRollHandler(RollService rollService) {
		this.rollService = rollService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.ROLL;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return TavernCommands.RollUsages.SIDES.equals(usage);
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		try {
			int sides = Integer.parseInt(CollectionUtils.first(message.getArgs()).orElse("1"));
			int roll = rollService.rollSingle(sides);
			event.getChannel().sendMessage("You rolled a " + roll).queue();
			return new CommandResultBuilder().success().build();
		} catch (NumberFormatException ex) {
			event.getChannel().sendMessage("Arguments must be numbers").queue();
			return new CommandResultBuilder().error().build();
		} catch (IllegalArgumentException ex) {
			event.getChannel().sendMessage(ex.getMessage()).queue();
			return new CommandResultBuilder().error().build();
		}
	}
}
