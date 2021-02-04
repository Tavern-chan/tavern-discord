package com.asm.tavern.discord.discord.roll

import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull

class RollTideHandler implements CommandHandler {

	@Override
	Command getCommand() {
		TavernCommands.ROLL
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		TavernCommands.RollUsages.ROLL_TIDE == usage
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		event.getChannel().sendMessage('What are you doing step bro!?').queue()
		return new CommandResultBuilder().success().build()
	}

}
