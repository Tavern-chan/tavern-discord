package com.tavern.discord.drinks;

import com.tavern.discord.DiscordUtils;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.Mention;
import com.tavern.domain.model.drinks.DrinkService;
import com.tavern.domain.model.drinks.MemberDrinkResult;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.attribute.IMemberContainer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DrinkCommandHandler implements CommandHandler {
	private final DrinkService drinkService;

	public DrinkCommandHandler(DrinkService drinkService) {
		this.drinkService = drinkService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.DRINK;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		List<Member> members = DiscordUtils.getUsersVoiceChannel(event.getJDA(), event.getAuthor().getId())
			.map(IMemberContainer::getMembers)
			.orElse(new ArrayList<>());
		List<MemberDrinkResult> results = drinkService.drink(event.getMember(), members);
		event.getChannel().sendMessage(results.stream()
            .map(result -> String.format("%s take %s drinks!", new Mention(result.getMember()), result.getDrinks()))
            .collect(Collectors.joining("\n"))).queue();
		return new CommandResultBuilder().success().build();
	}
}
