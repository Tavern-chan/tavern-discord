package com.asm.tavern.discord.discord.roll


import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.roll.RollService
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import javax.annotation.Nonnull

class DefaultRollHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(DefaultRollHandler.class)

	private final RollService rollService

	DefaultRollHandler(RollService rollService) {
		this.rollService = rollService
	}

	@Override
	Command getCommand() {
		TavernCommands.ROLL
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		TavernCommands.RollUsages.DEFAULT == usage
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		int roll = rollService.rollSingle(6)
		event.getChannel().sendMessage("You rolled a " + roll).queue({-> logger.trace('Sent default roll result')}, {-> logger.error('Failed to send default roll result')})
		return new CommandResultBuilder().success().build()
	}

}
