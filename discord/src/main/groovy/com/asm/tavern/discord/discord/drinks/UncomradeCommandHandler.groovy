package com.asm.tavern.discord.discord.drinks

import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.drinks.ComradeService
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

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
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		comradeService.disable()
		event.getGuild().getSelfMember().modifyNickname(null).submit()
		new CommandResultBuilder().success().build()
	}
}
