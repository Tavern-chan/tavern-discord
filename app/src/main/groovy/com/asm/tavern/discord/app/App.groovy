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
import com.asm.tavern.discord.audio.ModeService
import com.asm.tavern.domain.model.audio.SongRepository
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.command.CommandHandlerRegistry
import com.asm.tavern.domain.model.drinks.ComradeService
import com.asm.tavern.domain.model.drinks.DrinkRepository
import com.asm.tavern.domain.model.drinks.DrinkService
import com.asm.tavern.domain.model.drinks.QuestService
import com.asm.tavern.domain.model.roll.RollService
import com.asm.tavern.domain.model.audio.SpotifyService
import controller.CommandController
import controller.TavernApiController
import net.dv8tion.jda.api.JDA
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.support.GenericApplicationContext

class App {
	private static final XLogger logger = XLoggerFactory.getXLogger(App.class)

	private static Discord discord

	private static AppConfig appConfig

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
			String discordClientId = args.size() > 5 ? "" : args[5]
			String discordClientSecret = args.size() > 5 ? "" : args[6]

			try{
				File tavernDiscordProperties = new File(appConfigLocation)

				tavernDiscordProperties.createNewFile()
				properties.setProperty("discordToken", discordToken)
				properties.setProperty("discordClientId", discordClientId)
				properties.setProperty("discordClientSecret", discordClientSecret)
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




		logger.info("Initializing application context")
		GenericApplicationContext applicationContext = new GenericApplicationContext()

		applicationContext.registerBean(Discord.class, () -> new Discord(
				appConfig.getDiscordToken(), commandParser, commandHandlerRegistry
		))

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
		Discord discord = applicationContext.getBean(Discord.class)
		//ModeService modeService = applicationContext.getBean(ModeService.class)


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
			.add(new WeaveSongCommandHandler(songService, audioService))
			.add(new PlayModeCommandHandler(songService, audioService))

		discord.start()
		this.discord = discord
		this.appConfig = appConfig
		TavernApiServer.start()

    }

	@ComponentScan(basePackages = ["controller", "configuration"])
	@SpringBootApplication(exclude = [DataSourceAutoConfiguration.class])
	static class TavernApiServer {

		/**
		 Intention of Web App
		 Allow user to login to webapp via discord authentication
		 Pass through commands sent from front end to the tavern instance the user exists in?
		 Get GuildId from authentication, send commands through DiscordListener through a new listener that passing through needed context for commands to work
		 Required info from front end - NEW MODEL
		 Discord auth so we know who the user is,
		 CommandString ie:$play
		 CommandArgs ie: songid  Array
		 Interactions
		 Using Discord auth, get users current voice channel(tavern instance)
		 Pass through command into discord listener with voice channel
		 Discord Listener will then act upon the command just like a messagereceived event/ button press event/ slash event. etc...
		 Responses
		 Responses should make it back to the front end so that the user is aware of any issues/responses normally sent into the text channel.
		 If something is wrong with a command, tavern should be sending it back to the web app instead of in the text channel.

		 UI
		 Webapp UI will contain a queue list, ui buttons for pause, skip, shuffle
		 UI might also contain a seek with the ability to $st x to match the seek. (NOT MVP)

		 Webapp UI will also contain + button to open a text entry
		 -Text Entry pop up contains buttons for play, playnext, weave

		 WebApp ui will probably have a command hamburger menu
		 -Menu
		 -play
		 -pn
		 -weave
		 -Turns off weave
		 -pm
		 -List of categories selectable to start pm
		 -songs
		 -list of songs with buttons to play/playnext

		 Webapp does not require all commands that the discord integration needs
		 Commands like (np, queue, songs) are unnecessary as the ui will provide that itself


		 MVP WebApp Frontend
		 Text entry playing of song with viewable now playing.

		 MVP API
		 Parse command from POST request,
		 Return song info with GET request


		 **/

		static void start(String[] args) {
			SpringApplication.run(TavernApiServer, args)
		}

		static JDA getJda(){
			return discord.getJda()
		}

		static String getDiscordClientId(){
			return appConfig.getDiscordClientId()
		}

		static String getDiscordClientSecret(){
			return appConfig.getDiscordClientSecret()
		}

		static String getRedirectURI() {
			return appConfig.getRedirectURI()
		}
	}
}
