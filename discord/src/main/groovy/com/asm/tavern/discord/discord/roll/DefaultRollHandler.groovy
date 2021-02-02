package com.asm.tavern.discord.discord.roll

import com.asm.tavern.domain.model.DomainRegistry
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.Command
import com.asm.tavern.domain.model.command.CommandArgumentUsage
import com.asm.tavern.domain.model.command.CommandHandler
import com.asm.tavern.domain.model.command.CommandMessage
import com.asm.tavern.domain.model.command.CommandResult
import com.asm.tavern.domain.model.command.CommandResultBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import javax.annotation.Nonnull

class DefaultRollHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(DefaultRollHandler.class)

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
		int roll = DomainRegistry.rollService().rollSingle(6)
		event.getChannel().sendMessage("You rolled a " + roll).queue({-> logger.trace('Sent default roll result')}, {-> logger.error('Failed to send default roll result')})
		return new CommandResultBuilder().success().build()
	}

}