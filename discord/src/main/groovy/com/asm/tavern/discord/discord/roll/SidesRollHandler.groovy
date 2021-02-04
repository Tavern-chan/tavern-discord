package com.asm.tavern.discord.discord.roll


import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.roll.RollService
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull

class SidesRollHandler implements CommandHandler {
	private final RollService rollService

	SidesRollHandler(RollService rollService) {
		this.rollService = rollService
	}

	@Override
	Command getCommand() {
		TavernCommands.ROLL
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		TavernCommands.RollUsages.SIDES == usage
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		int sides = Integer.parseInt(message.args.first())
		int roll = rollService.rollSingle(sides)
		event.getChannel().sendMessage("You rolled a " + roll).queue()
		return new CommandResultBuilder().success().build()
	}
}
