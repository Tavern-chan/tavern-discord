package com.asm.tavern.discord.discord

import com.asm.tavern.discord.discord.command.parser.CommandParser
import com.asm.tavern.domain.model.DomainRegistry
import com.asm.tavern.domain.model.command.Command
import com.asm.tavern.domain.model.command.CommandHandler
import com.asm.tavern.domain.model.command.CommandHandlerRegistry
import com.asm.tavern.domain.model.command.CommandMessage
import com.asm.tavern.domain.model.discord.GuildId
import com.asm.tavern.domain.model.discord.VoiceChannelId
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import javax.annotation.Nonnull

class DiscordListener extends ListenerAdapter {
	private static final XLogger logger = XLoggerFactory.getXLogger(DiscordListener.class)

	final String prefix
	final List<Command> commands
	final CommandHandlerRegistry commandHandlerRegistry
	final CommandParser parser

	DiscordListener(String prefix, List<Command> commands, CommandHandlerRegistry commandHandlerRegistry) {
		this.prefix = prefix
		this.commands = commands
		parser = new CommandParser(prefix, commands)
		this.commandHandlerRegistry = commandHandlerRegistry
	}

	@Override
	void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
		String message = event.getMessage().getContentRaw().trim()
		CommandMessage result = parser.parse(message)

		if (!result.commandList.isEmpty()) {
			if (!result.usage.canUse(event.getMember())) {
				event.getChannel().sendMessage("You do not have permissions for this command").queue()
				logger.debug("${event.getMember().getEffectiveName()} cannot use the command usage ${result.commandList.last().name} - ${result.usage.name}")
				return
			}

			CommandHandler handler = commandHandlerRegistry.getHandler(result)
			if (handler) {
				handler.handle(event, result)
			} else {
				logger.warn("No handler for command {} with usage {}", result.commandList.last().name, result.usage.name)
			}
		} else if (message.startsWith(prefix)) {
			logger.debug("No command for message '{}'", message)
		}
	}

	@Override
	void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
		// Leave if everyone else leaves the chat
		GuildId guildId = new GuildId(event.getGuild().getId())
		VoiceChannelId voiceChannelId = new VoiceChannelId(event.getChannelLeft().getId())
		if (event.getChannelLeft().members.size() == 1 && voiceChannelId == DomainRegistry.audioService().getCurrentChannel(guildId)) {
			DomainRegistry.audioService().stop(guildId)
			DomainRegistry.audioService().leave(event.getGuild())
		}
	}
}
