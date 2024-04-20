package com.tavern.app

import com.tavern.audio.LavaPlayerAudioService
import com.tavern.discord.Discord
import com.tavern.discord.audio.AddSongCommandHandler
import com.tavern.discord.audio.AddSongWithCategoryCommandHandler
import com.tavern.discord.audio.ClearCommandHandler
import com.tavern.discord.audio.JoinCommandHandler
import com.tavern.discord.audio.LeaveCommandHandler
import com.tavern.discord.audio.NowPlayingCommandHandler
import com.tavern.discord.audio.PauseCommandHandler
import com.tavern.discord.audio.PlayCommandHandler
import com.tavern.discord.audio.PlayModeCommandHandler
import com.tavern.discord.audio.PlayNextCommandHandler
import com.tavern.discord.audio.QueueCommandHandler
import com.tavern.discord.audio.RemoveSongCommandHandler
import com.tavern.discord.audio.ShuffleQueueCommandHandler
import com.tavern.discord.audio.SkipCommandHandler
import com.tavern.discord.audio.SkipTimeCommandHandler
import com.tavern.discord.audio.SongsCommandHandler
import com.tavern.discord.audio.StopCommandHandler
import com.tavern.discord.audio.UnpauseCommandHandler
import com.tavern.discord.audio.WeaveSongCommandHandler
import com.tavern.discord.command.parser.CommandParser
import com.tavern.discord.drinks.ComradeCommandHandler
import com.tavern.discord.drinks.DrinkCommandHandler
import com.tavern.discord.drinks.DrinksCommandHandler
import com.tavern.discord.drinks.PopPopCommandHandler
import com.tavern.discord.drinks.UncomradeCommandHandler
import com.tavern.discord.help.CommandHelpHandler
import com.tavern.discord.help.HelpHandler
import com.tavern.drinks.LocalComradeService
import com.tavern.drinks.LocalDrinkRepository
import com.tavern.drinks.LocalDrinkService
import com.tavern.drinks.LocalQuestService
import com.tavern.repository.file.FileSongRepository
import com.tavern.domain.model.DomainRegistry
import com.tavern.domain.model.TavernCommands
import com.tavern.domain.model.audio.AudioService
import com.tavern.audio.ModeService
import com.tavern.domain.model.audio.SongRepository
import com.tavern.domain.model.audio.SongService
import com.tavern.domain.model.command.CommandHandlerRegistry
import com.tavern.domain.model.drinks.ComradeService
import com.tavern.domain.model.drinks.DrinkRepository
import com.tavern.domain.model.drinks.DrinkService
import com.tavern.domain.model.drinks.QuestService
import com.tavern.domain.model.roll.RollService
import com.tavern.domain.model.audio.SpotifyService
import com.tavern.discord.roll.AmountAndSidesRollHandler
import com.tavern.discord.roll.AmountXSidesHandler
import com.tavern.discord.roll.DefaultRollHandler
import com.tavern.discord.roll.RollTideHandler
import com.tavern.discord.roll.SidesRollHandler
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory
import org.springframework.context.support.GenericApplicationContext

class App {
	private static final XLogger logger = XLoggerFactory.getXLogger(App.class)

	static void main(String[] args) {
		logger.info("Starting up")
		String appConfigLocation = './tavern-discord.properties'
		Properties properties = new Properties()

		if (args && args.size() == 1) {
			appConfigLocation = args[0]
			properties.load(new FileInputStream(new File(appConfigLocation)))
		}
		else if(args.size() > 1){
			String discordToken = args[0]
			String songFileLocation =  args[1]
			String commandPrefix = args.size() == 2 ? "\$" : args[2]
			String spotifyClientId = args.size() > 4 ? "" : args[3]
			String spotifyClientSecret = args.size() > 4 ? "" : args[4]

			try{
				File tavernDiscordProperties = new File(appConfigLocation)

				tavernDiscordProperties.createNewFile()
				properties.setProperty("discordToken", discordToken)
				properties.setProperty("songFileLocation", songFileLocation)
				properties.setProperty("command.prefix", commandPrefix)
				properties.setProperty("spotifyClientId", spotifyClientId)
				properties.setProperty("spotifyClientSecret", spotifyClientSecret)
				properties.store(new FileOutputStream(tavernDiscordProperties), null)
				properties.load(new FileInputStream(new File(appConfigLocation)))
			}
			catch (Exception e){
				logger.error("There was an issue creating the tavern-discord.properties: ${e}" )
			}
		}
		else
			properties.load(new FileInputStream(new File(appConfigLocation)))

		AppConfig appConfig = new AppConfig(properties)
		logger.info("Reading configuration file at ${appConfigLocation}")
		properties.load(new FileInputStream(new File(appConfigLocation)))

		logger.info("Initializing Discord API")
		CommandHandlerRegistry commandHandlerRegistry = new CommandHandlerRegistry()
		CommandParser commandParser = new CommandParser(appConfig.getPrefix(), TavernCommands.getCommands())

		if(appConfig.getDiscordToken() == null || appConfig.getDiscordToken().isEmpty()){
			logger.info("DiscordToken is empty, please enter now:")
			properties.setProperty("discordToken", new Scanner(System.in).next())
			properties.store(new FileOutputStream(appConfigLocation), null)
		}
		Discord discord = new Discord(appConfig.getDiscordToken(), commandParser, commandHandlerRegistry)

		logger.info("Initializing application context")
		GenericApplicationContext applicationContext = new GenericApplicationContext()
		applicationContext.registerBean(RollService.class, RollService::new)

		applicationContext.registerBean(SongRepository.class, () -> new FileSongRepository(
				appConfig.getSongFile()
		))
		applicationContext.registerBean(SongService.class, () -> new SongService(
				applicationContext.getBean(SongRepository.class),
				applicationContext.getBean(SpotifyService.class)
		))
		applicationContext.registerBean(AudioService.class, () -> new LavaPlayerAudioService(
				applicationContext.getBean(ModeService.class)
		))
		applicationContext.registerBean(ModeService.class, () -> new ModeService(
				applicationContext.getBean(SongRepository.class)
		))
		applicationContext.registerBean(SpotifyService.class, () -> new SpotifyService(
				appConfig.getSpotifyClientId(), appConfig.getSpotifyClientSecret()
		))
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
			.add(new SkipTimeCommandHandler(audioService))
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
			.add(new WeaveSongCommandHandler(songService, audioService))
			.add(new PlayModeCommandHandler(songService, audioService))

		discord.start()
    }
}
