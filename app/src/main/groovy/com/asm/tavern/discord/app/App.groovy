package com.asm.tavern.discord.app

import com.asm.tavern.discord.audio.LavaPlayerAudioService
import com.asm.tavern.discord.discord.Discord
import com.asm.tavern.discord.discord.audio.*
import com.asm.tavern.discord.discord.command.parser.CommandParser
import com.asm.tavern.discord.discord.drinks.ComradeCommandHandler
import com.asm.tavern.discord.discord.drinks.DrinkCommandHandler
import com.asm.tavern.discord.discord.drinks.DrinksCommandHandler
import com.asm.tavern.discord.discord.drinks.PopPopCommandHandler
import com.asm.tavern.discord.discord.drinks.UncomradeCommandHandler
import com.asm.tavern.discord.discord.help.CommandHelpHandler
import com.asm.tavern.discord.discord.help.HelpHandler
import com.asm.tavern.discord.discord.roll.*
import com.asm.tavern.discord.drinks.LocalComradeService
import com.asm.tavern.discord.drinks.LocalDrinkRepository
import com.asm.tavern.discord.drinks.LocalDrinkService
import com.asm.tavern.discord.drinks.LocalQuestService
import com.asm.tavern.discord.repository.file.FileSongRepository
import com.asm.tavern.domain.model.DomainRegistry
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.audio.SongRepository
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.command.CommandHandlerRegistry
import com.asm.tavern.domain.model.drinks.ComradeService
import com.asm.tavern.domain.model.drinks.DrinkRepository
import com.asm.tavern.domain.model.drinks.DrinkService
import com.asm.tavern.domain.model.drinks.QuestService
import com.asm.tavern.domain.model.roll.RollService
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory
import org.springframework.context.support.GenericApplicationContext

class App {
	private static final XLogger logger = XLoggerFactory.getXLogger(App.class)

    static void main(String[] args) {
		logger.info("Starting up")
		String appConfigLocation = './tavern-discord.properties'
		if (args) {
			appConfigLocation = args[0]
		}
		logger.info("Reading configuration file at ${appConfigLocation}")
		Properties properties = new Properties()
		properties.load(new FileInputStream(new File(appConfigLocation)))
		AppConfig appConfig = new AppConfig(properties)

		logger.info("Initializing Discord API")
		CommandHandlerRegistry commandHandlerRegistry = new CommandHandlerRegistry()
		CommandParser commandParser = new CommandParser(appConfig.getPrefix(), TavernCommands.getCommands())
		Discord discord = new Discord(appConfig.getDiscordToken(), commandParser, commandHandlerRegistry)

		logger.info("Initializing application context")
		GenericApplicationContext applicationContext = new GenericApplicationContext()
		applicationContext.registerBean(RollService.class, RollService::new)
		applicationContext.registerBean(AudioService.class, LavaPlayerAudioService::new)
		applicationContext.registerBean(SongRepository.class, () -> new FileSongRepository(appConfig.getSongFile()))
		applicationContext.registerBean(SongService.class, () -> new SongService(applicationContext.getBean(SongRepository.class)))
		applicationContext.registerBean(ComradeService.class, LocalComradeService::new)
		applicationContext.registerBean(DrinkRepository.class, LocalDrinkRepository::new)
		applicationContext.registerBean(QuestService.class, LocalQuestService::new)
		applicationContext.registerBean(DrinkService.class, () -> new LocalDrinkService(
				applicationContext.getBean(DrinkRepository.class),
				applicationContext.getBean(QuestService.class),
				applicationContext.getBean(ComradeService.class)
		))

		logger.info("Starting application context")
		applicationContext.refresh()
		applicationContext.start()
		new DomainRegistry().setApplicationContext(applicationContext)

		RollService rollService = applicationContext.getBean(RollService.class)
		DrinkService drinkService = applicationContext.getBean(DrinkService.class)
		AudioService audioService = applicationContext.getBean(AudioService.class)
		SongService songService = applicationContext.getBean(SongService.class)

		logger.info("Loading in command handlers")
		commandHandlerRegistry
			.add(new DefaultRollHandler(rollService))
			.add(new SidesRollHandler(rollService))
			.add(new AmountAndSidesRollHandler(rollService))
			.add(new AmountXSidesHandler(rollService))
			.add(new RollTideHandler())
			.add(new HelpHandler(discord.commandParser.prefix))
			.add(new CommandHelpHandler(discord.commandParser))
			.add(new PlayCommandHandler(songService, audioService))
			.add(new NowPlayingCommandHandler(audioService))
			.add(new StopCommandHandler(audioService))
			.add(new SkipCommandHandler(audioService))
			.add(new PauseCommandHandler(audioService))
			.add(new UnpauseCommandHandler(audioService))
			.add(new JoinCommandHandler(audioService))
			.add(new LeaveCommandHandler(audioService))
			.add(new SongsCommandHandler(songService))
			.add(new AddSongCommandHandler(songService))
			.add(new AddSongWithCategoryCommandHandler(songService))
			.add(new RemoveSongCommandHandler(songService))
			.add(new QueueCommandHandler(audioService))
			.add(new ClearCommandHandler(audioService))
			.add(new ComradeCommandHandler(drinkService.getComradeService(), audioService))
			.add(new UncomradeCommandHandler(drinkService.getComradeService()))
			.add(new DrinkCommandHandler(drinkService))
			.add(new PopPopCommandHandler(drinkService))
			.add(new DrinksCommandHandler(drinkService))
			.add(new ShuffleQueueCommandHandler(audioService))
			.add(new PlayNextCommandHandler(songService, audioService))

		discord.start()
    }

}
