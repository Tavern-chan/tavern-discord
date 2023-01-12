package com.asm.tavern.discord.discord.drinks

import com.asm.tavern.discord.utilities.DiscordUtils
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.discord.Mention
import com.asm.tavern.domain.model.drinks.DrinkService
import com.asm.tavern.domain.model.drinks.MemberDrinkResult
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

import javax.annotation.Nonnull
import java.util.stream.Collectors

class PopPopCommandHandler implements CommandHandler {
	private final DrinkService drinkService

	PopPopCommandHandler(DrinkService drinkService) {
		this.drinkService = drinkService
	}

	@Override
	Command getCommand() {
		TavernCommands.POPPOP
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		DiscordUtils.getUsersVoiceChannel(event.getJDA(), event.getAuthor().id)
				.map(channel -> {
					List<MemberDrinkResult> results = drinkService.popPop(channel.members)
					if (results[0].shot) {
						event.getChannel().sendMessage("Pop Pop!\nSucks to suck. Everyone take a shot!").queue()
					} else {
						event.getChannel().sendMessage("Pop Pop!\n" + String.join("\n", results.stream().map(result -> "${new Mention(result.member.id)} take ${result.drinks} drinks!").collect(Collectors.toList()))).queue()
					}
					new CommandResultBuilder().success().build()
				}).orElse(_ -> {
					event.getChannel().sendMessage("Must be in a voice chat to pop pop")
					new CommandResultBuilder().error().build()
				})
	}

	@Override
	CommandResult handle(@Nonnull SlashCommandInteractionEvent event, CommandMessage message) {
		DiscordUtils.getUsersVoiceChannel(event.getJDA(), event.getAuthor().id)
				.map(channel -> {
					List<MemberDrinkResult> results = drinkService.popPop(channel.members)
					if (results[0].shot) {
						event.getChannel().sendMessage("Pop Pop!\nSucks to suck. Everyone take a shot!").queue()
					} else {
						event.getChannel().sendMessage("Pop Pop!\n" + String.join("\n", results.stream().map(result -> "${new Mention(result.member.id)} take ${result.drinks} drinks!").collect(Collectors.toList()))).queue()
					}
					new CommandResultBuilder().success().build()
				}).orElse(_ -> {
			event.getChannel().sendMessage("Must be in a voice chat to pop pop")
			new CommandResultBuilder().error().build()
		})
	}
}
