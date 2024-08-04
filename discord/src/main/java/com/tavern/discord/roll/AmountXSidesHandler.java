package com.tavern.discord.roll;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.roll.RollService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.StringJoiner;

public class AmountXSidesHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(AmountXSidesHandler.class);

	private final RollService rollService;

	public AmountXSidesHandler(RollService rollService) {
		this.rollService = rollService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.ROLL;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return TavernCommands.RollUsages.AMOUNT_X_SIDES.equals(usage);
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		try {
			String[] amountAndSides = message.getArgs().get(0).toLowerCase().split("x");
			int amount = Integer.parseInt(amountAndSides[0]);
			int sides = Integer.parseInt(amountAndSides[1]);

			final List<Integer> rolls = rollService.roll(amount, sides);
			final StringJoiner joiner = new StringJoiner("+");
			rolls.forEach(roll -> joiner.add(" " + roll + " "));
			String result = String.format(
				"You rolled: %s = %s",
				joiner,
				rolls.stream().reduce(0, Integer::sum)
			);
			event.getChannel().sendMessage(result).queue(
				__ -> logger.trace("Sent default roll result"),
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
