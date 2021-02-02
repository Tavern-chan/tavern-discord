package com.asm.tavern.discord.discord.help

import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.Command
import com.asm.tavern.domain.model.command.CommandArgumentUsage
import com.asm.tavern.domain.model.command.CommandHandler
import com.asm.tavern.domain.model.command.CommandMessage
import com.asm.tavern.domain.model.command.CommandResult
import com.asm.tavern.domain.model.command.CommandResultBuilder
import com.asm.tavern.domain.model.help.CommandIniPrinter
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull

class HelpHandler implements CommandHandler {

	@Override
	Command getCommand() {
		TavernCommands.HELP
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		TavernCommands.HELP.argumentUsages[0]
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		event.getChannel().sendMessage(new CommandIniPrinter().print(TavernCommands.getCommands())).queue()
		return new CommandResultBuilder().success().build()
	}

}
