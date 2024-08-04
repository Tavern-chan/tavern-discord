package com.tavern.discord;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tavern.discord.command.parser.CommandParser;
import com.tavern.domain.model.command.CommandHandlerRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.concurrent.Executors;

public class Discord {
	private static final XLogger logger = XLoggerFactory.getXLogger(Discord.class);
	private final String token;
	private final CommandHandlerRegistry commandHandlerRegistry;
	private final CommandParser commandParser;
	private JDA jda;

	public Discord(String token, CommandParser commandParser, CommandHandlerRegistry commandHandlerRegistry) {
		this.token = token;
		this.commandParser = commandParser;
		this.commandHandlerRegistry = commandHandlerRegistry;
	}

	public void start() {
		if (null == jda) {
			logger.info("Connecting to Discord");
			jda = JDABuilder.createDefault(token).setActivity(Activity.listening("Beer")).setEventPool(Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("JDA Thread %d").build())).addEventListeners(new DiscordListener(commandParser, commandHandlerRegistry)).enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS).build();
		}
	}

	public JDA getJda() {
		return jda;
	}

	public CommandParser getCommandParser() {
		return commandParser;
	}

	public final String getToken() {
		return token;
	}

	public final CommandHandlerRegistry getCommandHandlerRegistry() {
		return commandHandlerRegistry;
	}

}
