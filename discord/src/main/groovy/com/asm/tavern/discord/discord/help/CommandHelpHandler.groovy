package com.asm.tavern.discord.discord.help

import com.asm.tavern.discord.discord.command.parser.CommandParser
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.*
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import javax.annotation.Nonnull

class CommandHelpHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(CommandHelpHandler.class)
	private CommandParser commandParser

	CommandHelpHandler(CommandParser commandParser) {
		this.commandParser = commandParser
	}

	@Override
	Command getCommand() {
		TavernCommands.HELP
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		TavernCommands.HelpUsages.COMMAND_HELP == usage
	}

	@Override
	CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		String commandMessage = "${commandParser.prefix}${String.join(" ", message.args)}"
		CommandMessage commandToHelpWith = commandParser.getCommandFromMessage(commandMessage)
		if (commandToHelpWith.commandList.isEmpty()) {
			logger.error("Command Help could not find the provided command")
			return new CommandResultBuilder().error().build()
		}

		Command command = commandToHelpWith.commandList.last()
		StringBuilder text = new StringBuilder("${commandParser.prefix}${commandToHelpWith.getCommandString()} - ${command.description}\n")

		if (!command.argumentUsages.isEmpty()) {
			command.argumentUsages.forEach(usage -> {
				StringBuilder usageText = new StringBuilder("```\n")
						.append("${usage.description}\n")
						.append("${commandParser.prefix}${commandToHelpWith.getCommandString()}")
				usage.args.forEach(arg -> usageText.append(" <${arg.name}>"))
				if (!usage.args.isEmpty()) {
					usageText.append("\n")
					usage.args.forEach(arg -> usageText.append("${arg.name} - ${arg.description}\n"))
					usageText.append("\n")
				}
				text.append(usageText.append("```").append("\n").toString())
			})
		}

		if (!command.subCommands.isEmpty()) {
			StringBuilder subCommandText = new StringBuilder("```\n")
					.append("Sub Commands:\n")

			command.subCommands.forEach(subCommand -> {
				subCommandText.append("${commandParser.prefix}${commandToHelpWith.getCommandString()} ${subCommand.name} - ${subCommand.description}\n")
			})

			text.append(subCommandText.append("```").toString())
		}

		event.getChannel().sendMessage(text.toString()).queue()

		new CommandResultBuilder().success().build()
	}

	@Override
	CommandResult handle(@Nonnull SlashCommandInteractionEvent event, CommandMessage message) {
		String commandMessage = "${commandParser.prefix}${String.join(" ", message.args)}"
		CommandMessage commandToHelpWith = commandParser.getCommandFromMessage(commandMessage)
		if (commandToHelpWith.commandList.isEmpty()) {
			logger.error("Command Help could not find the provided command")
			return new CommandResultBuilder().error().build()
		}

		Command command = commandToHelpWith.commandList.last()
		StringBuilder text = new StringBuilder("${commandParser.prefix}${commandToHelpWith.getCommandString()} - ${command.description}\n")

		if (!command.argumentUsages.isEmpty()) {
			command.argumentUsages.forEach(usage -> {
				StringBuilder usageText = new StringBuilder("```\n")
						.append("${usage.description}\n")
						.append("${commandParser.prefix}${commandToHelpWith.getCommandString()}")
				usage.args.forEach(arg -> usageText.append(" <${arg.name}>"))
				if (!usage.args.isEmpty()) {
					usageText.append("\n")
					usage.args.forEach(arg -> usageText.append("${arg.name} - ${arg.description}\n"))
					usageText.append("\n")
				}
				text.append(usageText.append("```").append("\n").toString())
			})
		}

		if (!command.subCommands.isEmpty()) {
			StringBuilder subCommandText = new StringBuilder("```\n")
					.append("Sub Commands:\n")

			command.subCommands.forEach(subCommand -> {
				subCommandText.append("${commandParser.prefix}${commandToHelpWith.getCommandString()} ${subCommand.name} - ${subCommand.description}\n")
			})

			text.append(subCommandText.append("```").toString())
		}

		event.getChannel().sendMessage(text.toString()).queue()

		new CommandResultBuilder().success().build()
	}
}
