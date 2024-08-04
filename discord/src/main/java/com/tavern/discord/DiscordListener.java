package com.tavern.discord;

import com.google.common.collect.Iterables;
import com.tavern.discord.command.parser.CommandParser;
import com.tavern.domain.model.DomainRegistry;
import com.tavern.domain.model.command.CommandHandler;
import com.tavern.domain.model.command.CommandHandlerRegistry;
import com.tavern.domain.model.command.CommandMessage;
import com.tavern.domain.model.discord.GuildId;
import com.tavern.domain.model.discord.VoiceChannelId;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nonnull;

public class DiscordListener extends ListenerAdapter {
	private static final XLogger logger = XLoggerFactory.getXLogger(DiscordListener.class);

	private final CommandHandlerRegistry commandHandlerRegistry;
	private final CommandParser parser;

	public DiscordListener(CommandParser parser, CommandHandlerRegistry commandHandlerRegistry) {
		this.commandHandlerRegistry = commandHandlerRegistry;
		this.parser = parser;
	}

	public final CommandHandlerRegistry getCommandHandlerRegistry() {
		return commandHandlerRegistry;
	}

	public final CommandParser getParser() {
		return parser;
	}

	@Override
	public void onMessageReceived(@Nonnull final MessageReceivedEvent event) {
		String message = event.getMessage().getContentRaw().trim();
		final CommandMessage result = parser.parse(message);

		if (event.getAuthor().isBot()) {
			logger.trace("Throwing out bot message");
			return;
		}

		if (!result.getCommandList().isEmpty()) {
			if (!result.getUsage().canUse(event.getMember())) {
				event.getChannel().sendMessage("You do not have permissions for this command").queue();
				logger.debug(
					"{} cannot se the command usage {} - {}",
					event.getMember().getEffectiveName(),
					Iterables.getLast(result.getCommandList(), null).getName(),
					result.getUsage().getName()
				);
				return;
			}

			CommandHandler handler = commandHandlerRegistry.getHandler(result);
			if (null == handler) {
				logger.warn("No handler for command {} with usage {}", Iterables.getLast(result.getCommandList()).getName(), result.getUsage().getName());
			} else {
				if (!handler.handle(event, result).success()) {
					logger.error("Failed to handle command {}{}", getParser().getPrefix(), result.getCommandString());
				}
			}
		} else if (message.startsWith(parser.getPrefix())) {
			logger.debug("No command for message '{}'", message);
		}
	}

	@Override
	public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
		// Leave if everyone else leaves the chat
		GuildId guildId = new GuildId(event.getGuild().getId());
		AudioChannelUnion channel = event.getChannelLeft();
		if (null != channel) {
			VoiceChannelId voiceChannelId = new VoiceChannelId(channel.getId());
			if (channel.getMembers().size() == 1 && voiceChannelId.equals(DomainRegistry.audioService().getCurrentChannel(guildId))) {
				DomainRegistry.audioService().stop(guildId);
				DomainRegistry.audioService().leave(event.getGuild());
			}
		}
	}

	@Override
	public void onButtonInteraction(@Nonnull final ButtonInteractionEvent event) {
		String message = event.getButton().getId();
		final CommandMessage result = parser.parse(parser.getPrefix() + message);
		event.deferEdit().queue();

		if (!result.getCommandList().isEmpty()) {
			if (!result.getUsage().canUse(event.getMember())) {
				event.getChannel().sendMessage("You do not have permissions for this command").queue();
				logger.debug("{} cannot use the command usage {} - {}", event.getMember().getEffectiveName(), Iterables.getLast(result.getCommandList()).getName(), result.getUsage().getName());
				return;
			}

			CommandHandler handler = commandHandlerRegistry.getHandler(result);
			if (null == handler) {
				logger.warn("No handler for command {} with usage {}", Iterables.getLast(result.getCommandList()).getName(), result.getUsage().getName());
			} else {
				if (!handler.handle(new MessageReceivedEvent(event.getJDA(), event.getResponseNumber(), event.getMessage()), result).success()) {
					logger.error("Failed to handle command {}{}", getParser().getPrefix(), result.getCommandString());
				}
			}

		} else if (message.startsWith(parser.getPrefix())) {
			logger.debug("No command for message '{}'", message);
		}
	}
}
