package com.tavern.discord.drinks;

import com.tavern.discord.DiscordUtils;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.command.*;
import com.tavern.domain.model.discord.Mention;
import com.tavern.domain.model.drinks.DrinkService;
import com.tavern.domain.model.drinks.MemberDrinkResult;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class PopPopCommandHandler implements CommandHandler {
	private final DrinkService drinkService;

	public PopPopCommandHandler(DrinkService drinkService) {
		this.drinkService = drinkService;
	}

	@Override
	public Command getCommand() {
		return TavernCommands.POPPOP;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		return DiscordUtils.getUsersVoiceChannel(event.getJDA(), event.getAuthor().getId())
			.map(channel -> {
				List<MemberDrinkResult> results = drinkService.popPop(channel.getMembers());
				if (results.get(0).getShot()) {
					event.getChannel().sendMessage("Pop Pop!\nSucks to suck. Everyone take a shot!").queue();
				} else {
					event.getChannel().sendMessage("Pop Pop!\n" + results.stream().map(result -> String.format("%s take %s drinks!", new Mention(result.getMember()), result.getDrinks())).collect(Collectors.joining("\n"))).queue();
				}
				return new CommandResultBuilder().success().build();
			}).orElseGet(() -> {
				event.getChannel().sendMessage("Must be in a voice chat to pop pop").queue();
				return new CommandResultBuilder().error().build();
			});
	}

}
