package com.asm.tavern.discord.discord

import com.asm.tavern.domain.model.command.Command
import com.asm.tavern.domain.model.command.CommandHandlerRegistry
import com.google.common.util.concurrent.ThreadFactoryBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import java.util.concurrent.Executors

class Discord {
	private static final XLogger logger = XLoggerFactory.getXLogger(Discord.class)

	final String token
	final String prefix
	final List<Command> commands
	final CommandHandlerRegistry commandHandlerRegistry
	private JDA jda

	Discord(String token, String prefix, List<Command> commands, CommandHandlerRegistry commandHandlerRegistry) {
		this.token = token
		this.prefix = prefix
		this.commands = commands
		this.commandHandlerRegistry = commandHandlerRegistry
	}

	void start() {
		if (null == jda) {
			logger.info("Connecting to Discord")
			jda = JDABuilder.createDefault(token)
					.setActivity(Activity.listening("Beer"))
					.setEventPool(Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("JDA Thread %d").build()))
					.addEventListeners(new DiscordListener(prefix, commands, commandHandlerRegistry))
					.build()
		}
	}

	JDA getJda() {
		jda
	}

}
