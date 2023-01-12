package com.asm.tavern.discord.discord.drinks

import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.drinks.ComradeService
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

import javax.annotation.Nonnull

class UncomradeCommandHandler implements CommandHandler {
	private final ComradeService comradeService

	UncomradeCommandHandler(ComradeService comradeService) {
		this.comradeService = comradeService
	}

	@Override
	Command getCommand() {
		TavernCommands.UNCOMRADE
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		comradeService.disable()
		event.getGuild().getSelfMember().modifyNickname(null).submit()
		new CommandResultBuilder().success().build()
	}

	@Override
	CommandResult handle(@Nonnull SlashCommandInteractionEvent event, CommandMessage message) {
		comradeService.disable()
		event.getGuild().getSelfMember().modifyNickname(null).submit()
		new CommandResultBuilder().success().build()
	}
}
