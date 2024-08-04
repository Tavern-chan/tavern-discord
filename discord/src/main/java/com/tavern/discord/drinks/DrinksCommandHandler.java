package com.tavern.discord.drinks;

import com.tavern.discord.DiscordUtils;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.Mention;
import com.tavern.domain.model.discord.UserId;
import com.tavern.domain.model.drinks.DrinkService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.attribute.IMemberContainer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DrinksCommandHandler implements CommandHandler {
	private final DrinkService drinkService;

	public DrinksCommandHandler(DrinkService drinkService) {
		this.drinkService = drinkService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.DRINKS;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		List<Member> members = DiscordUtils.getUsersVoiceChannel(event.getJDA(), event.getMember().getId())
			.map(IMemberContainer::getMembers)
			.orElse(new ArrayList<>(Collections.singleton(event.getMember())));

		String text = String.format(
			"Drink Totals\n%s",
			members.stream()
				.map(member -> String.format(
					"%s: %s",
					new Mention(member),
					drinkService.getDrinkRepository().getDrinks(new UserId(member.getId()))
				))
				.collect(Collectors.toList())
		);
		event.getChannel().sendMessage(text).queue();
		return new CommandResultBuilder().success().build();
	}
}
