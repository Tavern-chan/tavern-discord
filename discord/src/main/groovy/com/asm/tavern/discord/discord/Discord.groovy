package com.asm.tavern.discord.discord

import com.asm.tavern.discord.discord.command.parser.CommandParser
import com.asm.tavern.domain.model.command.CommandHandlerRegistry
import com.google.common.util.concurrent.ThreadFactoryBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory
import com.asm.tavern.domain.model.TavernCommands

import java.util.concurrent.Executors

class Discord {
	private static final XLogger logger = XLoggerFactory.getXLogger(Discord.class)

	final String token
	final CommandHandlerRegistry commandHandlerRegistry
	final CommandParser commandParser
	private JDA jda

	Discord(String token, CommandParser commandParser, CommandHandlerRegistry commandHandlerRegistry) {
		this.token = token
		this.commandParser = commandParser
		this.commandHandlerRegistry = commandHandlerRegistry
	}

	void start() {
		if (null == jda) {
			logger.info("Connecting to Discord")
			jda = JDABuilder.createDefault(token)
					.setActivity(Activity.listening("Beer"))
					.setEventPool(Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("JDA Thread %d").build()))
					.addEventListeners(new DiscordListener(commandParser, commandHandlerRegistry))
					.enableIntents(GatewayIntent.GUILD_VOICE_STATES)
					// In order to continue viewing message content, discord now requires you to explicitly enable MESSAGE_CONTENT intent. This allows legacy commands: $play etc..
					.enableIntents(GatewayIntent.MESSAGE_CONTENT)
					.build()
		}
		initCommands()
	}

	void initCommands(){

		// Just a simple != length of commandlist for now, ideally this should probably use some kind of last updated or verifying that the command list matches...
		if(jda.retrieveCommands().complete().size() != TavernCommands.getCommands().size()){
			List<CommandData> commandDataList = new ArrayList<>()

			TavernCommands.getCommands().forEach({ command ->
				SlashCommandData slashCommandData = Commands.slash(command.name, command.description)
				command.argumentUsages.forEach({arg ->
					if(arg.name == "default"){
						//slashCommandData.addOption(OptionType.)
					}
					else if(!arg.name.contains(' '))
						slashCommandData.addOption(OptionType.STRING, arg.name, arg.description)

				})
				commandDataList.add(slashCommandData)
			})
			jda.updateCommands().addCommands(commandDataList).queue()
		}
	}

	JDA getJda() {
		jda
	}

	CommandParser getCommandParser() {
		commandParser
	}

}
