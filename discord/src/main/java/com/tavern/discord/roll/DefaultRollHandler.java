package com.tavern.discord.roll;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.roll.RollService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;

public class DefaultRollHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(DefaultRollHandler.class);

	private final RollService rollService;

	public DefaultRollHandler(RollService rollService) {
		this.rollService = rollService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.ROLL;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return TavernCommands.RollUsages.DEFAULT.equals(usage);
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		int roll = rollService.rollSingle(6);
		event.getChannel().sendMessage("You rolled a " + roll)
			.queue(
				__ -> logger.trace("Sent default roll result"),
				ex -> logger.error("Failed to send default roll result", ex)
			);
		return new CommandResultBuilder().success().build();
	}

}
