package com.asm.tavern.discord.discord.roll


import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.roll.RollService
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import javax.annotation.Nonnull

class AmountAndSidesRollHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(AmountAndSidesRollHandler.class)

	private final RollService rollService

	AmountAndSidesRollHandler(RollService rollService) {
		this.rollService = rollService
	}

	@Override
	Command getCommand() {
		TavernCommands.ROLL
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		TavernCommands.RollUsages.AMOUNT_AND_SIDES == usage
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		int amount = Integer.parseInt(message.args.first())
		int sides = Integer.parseInt(message.args.last())

		List<Integer> rolls = rollService.roll(amount, sides)
		StringJoiner joiner = new StringJoiner('+')
		rolls.forEach({roll -> joiner.add(" ${roll} ")})
		String result = "You rolled: ${joiner.toString()} = ${rolls.stream().reduce(0, Integer::sum)}"
		event.getChannel().sendMessage(result).queue({-> logger.trace('Sent default roll result')}, {-> logger.error('Failed to send default roll result')})
		return new CommandResultBuilder().success().build()
	}

}
