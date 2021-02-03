package com.asm.tavern.discord.discord.drinks

import com.asm.tavern.discord.utilities.DiscordUtils
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.discord.Mention
import com.asm.tavern.domain.model.discord.UserId
import com.asm.tavern.domain.model.drinks.DrinkService
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull
import java.util.stream.Collectors

class DrinksCommandHandler implements CommandHandler {
	private final DrinkService drinkService

	DrinksCommandHandler(DrinkService drinkService) {
		this.drinkService = drinkService
	}

	@Override
	Command getCommand() {
		TavernCommands.DRINKS
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		List<Member> members = DiscordUtils.getUsersVoiceChannel(event.getJDA(), event.getMember().id)
				.map(channel -> channel.members)
				.orElse([event.getMember()])

		event.getChannel().sendMessage("Drink totals\n" + String.join("\n", members.stream().map(member -> "${new Mention(member)}: ${drinkService.getDrinkRepository().getDrinks(new UserId(member.id))}").collect(Collectors.toList()))).queue()
		new CommandResultBuilder().success().build()
	}

}
