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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

import javax.annotation.Nonnull

class HelpHandler implements CommandHandler {
	private String prefix

	HelpHandler(String prefix) {
		this.prefix = prefix
	}

	@Override
	Command getCommand() {
		TavernCommands.HELP
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		TavernCommands.HelpUsages.DEFAULT == usage
	}

	@Override
	CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		event.getChannel().sendMessage(new CommandIniPrinter().print(prefix, TavernCommands.getCommands())).queue()
		return new CommandResultBuilder().success().build()
	}

	@Override
	CommandResult handle(@Nonnull SlashCommandInteractionEvent event, CommandMessage message) {
		event.getChannel().sendMessage(new CommandIniPrinter().print(prefix, TavernCommands.getCommands())).queue()
		return new CommandResultBuilder().success().build()
	}
}
