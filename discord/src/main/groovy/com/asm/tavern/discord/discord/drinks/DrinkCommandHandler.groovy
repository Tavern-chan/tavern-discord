package com.asm.tavern.discord.discord.drinks

import com.asm.tavern.discord.utilities.DiscordUtils
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.discord.Mention
import com.asm.tavern.domain.model.drinks.DrinkService
import com.asm.tavern.domain.model.drinks.MemberDrinkResult
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull
import java.util.stream.Collectors

class DrinkCommandHandler implements CommandHandler {
	private final DrinkService drinkService

	DrinkCommandHandler(DrinkService drinkService) {
		this.drinkService = drinkService
	}

	@Override
	Command getCommand() {
		TavernCommands.DRINK
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		List<Member> members = DiscordUtils.getUsersVoiceChannel(event.getJDA(), event.getAuthor().id)
				.map(channel -> channel.getMembers())
				.orElse([])
		List<MemberDrinkResult> results = drinkService.drink(event.getMember(), members)
		event.getChannel().sendMessage(String.join("\n", results.stream().map(result -> "${new Mention(result.member.id)} take ${result.drinks} drinks!").collect(Collectors.toList()))).queue()
		new CommandResultBuilder().success().build()
	}
}
