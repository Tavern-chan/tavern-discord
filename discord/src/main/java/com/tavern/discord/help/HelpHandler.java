package com.tavern.discord.help;

import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.help.CommandIniPrinter;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class HelpHandler implements CommandHandler {
	private String prefix;

	public HelpHandler(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.HELP;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return TavernCommands.HelpUsages.DEFAULT.equals(usage);
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		event.getChannel().sendMessage(new CommandIniPrinter().print(prefix, TavernCommands.getCommands())).queue();
		return new CommandResultBuilder().success().build();
	}

	/**
	 * Handle the invocation of the command
	 *
	 * @param event the discord event
	 * @param message the message
	 * @return result of the command
	 */
	public CommandResult handle(@Nonnull ButtonInteractionEvent event, CommandMessage message) {//todoA
		event.getChannel().sendMessage(new CommandIniPrinter().print(prefix, TavernCommands.getCommands())).queue();
		return new CommandResultBuilder().success().build();
	}
}
