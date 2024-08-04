package com.tavern.discord.help;

import com.google.common.collect.Iterables;
import com.tavern.discord.command.parser.CommandParser;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;

public class CommandHelpHandler implements CommandHandler {
	private static final XLogger logger = XLoggerFactory.getXLogger(CommandHelpHandler.class);

	private CommandParser commandParser;

	public CommandHelpHandler(CommandParser commandParser) {
		this.commandParser = commandParser;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.HELP;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return TavernCommands.HelpUsages.COMMAND_HELP.equals(usage);
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, final CommandMessage message) {
		String commandMessage = commandParser.getPrefix() + String.join(" ", message.getArgs());
		final CommandMessage commandToHelpWith = commandParser.getCommandFromMessage(commandMessage);
		if (commandToHelpWith.getCommandList().isEmpty()) {
			logger.error("Command Help could not find the provided command");
			return new CommandResultBuilder().error().build();
		}

		final Command command = Iterables.getLast(commandToHelpWith.getCommandList());
		StringBuilder text = new StringBuilder(commandParser.getPrefix() + commandToHelpWith.getCommandString() + " - " + command.getDescription() + "\n");

		if (!command.getArgumentUsages().isEmpty()) {
			command.getArgumentUsages().forEach(usage -> {
				StringBuilder usageText = new StringBuilder("```\n")
					.append(usage.getDescription())
					.append("\n")
					.append(commandParser.getPrefix())
					.append(commandToHelpWith.getCommandString());
				usage.getArgs().forEach(arg -> usageText.append(" <").append(arg.getName()).append(">"));
				if (!usage.getArgs().isEmpty()) {
					usageText.append("\n");
					usage.getArgs().forEach(arg -> usageText.append(arg.getName()).append(" - ").append(arg.getDescription()).append("\n"));
					usageText.append("\n");
				}
				text.append(usageText.append("```").append("\n"));
			});
		}

		if (!command.getSubCommands().isEmpty()) {
			StringBuilder subCommandText = new StringBuilder("```\n")
				.append("Sub Commands:\n");

			command.getSubCommands().forEach(subCommand ->
				subCommandText.append("commandParser.getPrefix()")
					.append(commandToHelpWith.getCommandString())
					.append(" ")
					.append(subCommand.getName())
					.append(" - ")
					.append(subCommand.getDescription())
					.append("\n")
			);

			text.append(subCommandText.append("```"));
		}

		event.getChannel().sendMessage(text.toString()).queue();
		return new CommandResultBuilder().success().build();
	}

}
