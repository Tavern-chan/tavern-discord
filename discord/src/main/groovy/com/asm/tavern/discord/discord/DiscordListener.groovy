package com.asm.tavern.discord.discord

import com.asm.tavern.discord.discord.command.parser.CommandParser
import com.asm.tavern.domain.model.DomainRegistry
import com.asm.tavern.domain.model.command.CommandHandler
import com.asm.tavern.domain.model.command.CommandHandlerRegistry
import com.asm.tavern.domain.model.command.CommandMessage
import com.asm.tavern.domain.model.discord.GuildId
import com.asm.tavern.domain.model.discord.VoiceChannelId
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.managers.AudioManager
import org.jetbrains.annotations.NotNull
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

	void routeCommand(String message, MessageReceivedEvent event){
		CommandMessage result = parser.parse(message)

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

	void routeCommand(String message, SlashCommandInteractionEvent event){
		CommandMessage result = parser.parse(message)

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
	void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		String message = event.getMessage().getContentRaw().trim()
		routeCommand(message, event)
	}

	@Override
	void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		// Slash commands are required to be responded to
		// additionally if they are not responded too within 3 seconds they fail.
		// You may defer replying which will allow more time for responding to the event
		// TODO Either deferReply and reply later. Should this be ephemeral or should it reply with the message received so that you can tell who played a song in tavern as normal?
		event.reply("heard").setEphemeral(true).queue()

		// Rather than rewrite command handling just reuse the message command handling. But to do so the message has to be the same as if $ was used.
		String message = parser.prefix + event.getName()
		if(event.getOptions())
			message = message + " " + event.getOptions()[0].getAsString()
		routeCommand(message, event)
	}

	@Override
	void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
		// Leave if everyone else leaves the chat
		if(event.getChannelLeft()){
			GuildId guildId = new GuildId(event.getGuild().getId())
			VoiceChannelId voiceChannelId = new VoiceChannelId(event.getChannelLeft().getId())
			if (event.getChannelLeft().members.size() == 1 && voiceChannelId == DomainRegistry.audioService().getCurrentChannel(guildId)) {
				DomainRegistry.audioService().stop(guildId)
				DomainRegistry.audioService().leave(event.getGuild())
			}
		}
	}
}
