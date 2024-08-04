package com.tavern.discord.roll;

import com.google.common.collect.Iterables;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.roll.RollService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.StringJoiner;

public class AmountAndSidesRollHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(AmountAndSidesRollHandler.class);

	private final RollService rollService;

	public AmountAndSidesRollHandler(RollService rollService) {
		this.rollService = rollService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.ROLL;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return TavernCommands.RollUsages.AMOUNT_AND_SIDES.equals(usage);
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		try {
			int amount = Integer.parseInt(Iterables.getFirst(message.getArgs(), "1"));
			int sides = Integer.parseInt(Iterables.getLast(message.getArgs(), "6"));

			final List<Integer> rolls = rollService.roll(amount, sides);
			final StringJoiner joiner = new StringJoiner("+");

			rolls.forEach(roll -> joiner.add(" " + roll + " "));

			String result = String.format("You rolled: %s = %s", joiner, rolls.stream().reduce(0, Integer::sum));
			event.getChannel().sendMessage(result).queue(
				__ -> logger.trace("Send default roll result"),
				ex -> logger.error("Failed to send default roll result", ex)
			);
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
