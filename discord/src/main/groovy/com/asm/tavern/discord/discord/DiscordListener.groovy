package com.asm.tavern.discord.discord

import com.asm.tavern.discord.discord.command.parser.CommandParser
import com.asm.tavern.domain.model.DomainRegistry
import com.asm.tavern.domain.model.command.CommandHandler
import com.asm.tavern.domain.model.command.CommandHandlerRegistry
import com.asm.tavern.domain.model.command.CommandMessage
import com.asm.tavern.domain.model.discord.GuildId
import com.asm.tavern.domain.model.discord.VoiceChannelId
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.events.interaction.*
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import javax.annotation.Nonnull

class DiscordListener extends ListenerAdapter {
	private static final XLogger logger = XLoggerFactory.getXLogger(DiscordListener.class)

	final CommandHandlerRegistry commandHandlerRegistry
	final CommandParser parser

	DiscordListener(CommandParser parser, CommandHandlerRegistry commandHandlerRegistry) {
		this.commandHandlerRegistry = commandHandlerRegistry
		this.parser = parser
	}


	//MessageChannelUnion messageChannelUnion, GuildId guildId, CommandMessage message
	// First two derived from user.
	String onApiCallReceived(TextChannel textChannel = null, Guild guild, Member member, String message) {
		CommandMessage result = parser.parse(parser.prefix + message)
		CommandHandler handler = commandHandlerRegistry.getHandler(result)


		if (handler) {
			if (!handler.handle(textChannel, guild, member,  result)) {
				logger.error("Failed to handle command ${parser.prefix}${result.getCommandString()}")
			}
		} else {
			logger.warn("No handler for command {} with usage {}", result.commandList.last().name, result.usage.name)
		}
	}

	@Override
	void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		String message = event.getMessage().getContentRaw().trim()
		CommandMessage result = parser.parse(message)

		if (event.author.bot) {
			logger.trace("Throwing out bot message")
			return
		}

		if (!result.commandList.isEmpty()) {
			if (!result.usage.canUse(event.getMember())) {
				event.getChannel().sendMessage("You do not have permissions for this command").queue()
				logger.debug("${event.getMember().getEffectiveName()} cannot use the command usage ${result.commandList.last().name} - ${result.usage.name}")
				return
			}

			CommandHandler handler = commandHandlerRegistry.getHandler(result)
			if (handler) {
				if (!handler.handle(event, result)) {
					logger.error("Failed to handle command ${parser.prefix}${result.getCommandString()}")
				}
			} else {
				logger.warn("No handler for command {} with usage {}", result.commandList.last().name, result.usage.name)
			}
		} else if (message.startsWith(parser.prefix)) {
			logger.debug("No command for message '{}'", message)
		}
	}

	@Override
	void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
		// Leave if everyone else leaves the chat
		GuildId guildId = new GuildId(event.getGuild().getId())
		VoiceChannelId voiceChannelId = new VoiceChannelId(event.getChannelLeft().getId())
		if (event.getChannelLeft().members.size() == 1 && voiceChannelId == DomainRegistry.audioService().getCurrentChannel(guildId)) {
			DomainRegistry.audioService().stop(guildId)
			DomainRegistry.audioService().leave(event.getGuild())
		}
	}

	@Override
	void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
		String message = event.getButton().id
		CommandMessage result = parser.parse(parser.prefix + message)
		event.deferEdit().queue()


		if (!result.commandList.isEmpty()) {
			if (!result.usage.canUse(event.getMember())) {
				event.getChannel().sendMessage("You do not have permissions for this command").queue()
				logger.debug("${event.getMember().getEffectiveName()} cannot use the command usage ${result.commandList.last().name} - ${result.usage.name}")
				return
			}

			CommandHandler handler = commandHandlerRegistry.getHandler(result)
			if (handler) {
				if (!handler.handle(new MessageReceivedEvent(event.getJDA(), event.responseNumber, event.getMessage()), result)) {
					logger.error("Failed to handle command ${parser.prefix}${result.getCommandString()}")
				}
			} else {
				logger.warn("No handler for command {} with usage {}", result.commandList.last().name, result.usage.name)
			}
		} else if (message.startsWith(parser.prefix)) {
			logger.debug("No command for message '{}'", message)
		}
	}
}
