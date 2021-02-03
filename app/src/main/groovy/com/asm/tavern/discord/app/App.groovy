package com.asm.tavern.discord.app

import com.asm.tavern.discord.audio.LavaPlayerAudioService
import com.asm.tavern.discord.discord.Discord
import com.asm.tavern.discord.discord.audio.*
import com.asm.tavern.discord.discord.drinks.ComradeCommandHandler
import com.asm.tavern.discord.discord.drinks.DrinkCommandHandler
import com.asm.tavern.discord.discord.drinks.DrinksCommandHandler
import com.asm.tavern.discord.discord.drinks.PopPopCommandHandler
import com.asm.tavern.discord.discord.drinks.UncomradeCommandHandler
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
	private static final String PREFIX = '$'

    static void main(String[] args) {
		String token = args[0]

		logger.info("Initializing Discord API")
		CommandHandlerRegistry commandHandlerRegistry = new CommandHandlerRegistry()
		Discord discord = new Discord(token, PREFIX, TavernCommands.getCommands(), commandHandlerRegistry)

		logger.info("Initializing application context") // probably don't need this: new ClassPathXmlApplicationContext("applicationContext.xml")
		GenericApplicationContext applicationContext = new GenericApplicationContext()
		applicationContext.registerBean(Discord.class, () -> discord)
		applicationContext.registerBean(RollService.class, RollService::new)
		applicationContext.registerBean(AudioService.class, LavaPlayerAudioService::new)
		applicationContext.registerBean(SongRepository.class, () -> new FileSongRepository(new File("E:/dev/test/songs.json"))) // TODO: Configurable storage path
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

		logger.info("Loading in command handlers")
		commandHandlerRegistry
			.add(new DefaultRollHandler())
			.add(new SidesRollHandler())
			.add(new AmountAndSidesRollHandler())
			.add(new AmountXSidesHandler())
			.add(new RollTideHandler())
			.add(new HelpHandler())
			.add(new PlayCommandHandler())
			.add(new NowPlayingCommandHandler())
			.add(new StopCommandHandler())
			.add(new SkipCommandHandler())
			.add(new PauseCommandHandler())
			.add(new UnpauseCommandHandler())
			.add(new JoinCommandHandler())
			.add(new LeaveCommandHandler())
			.add(new SongsCommandHandler(applicationContext.getBean(SongService.class)))
			.add(new AddSongCommandHandler(applicationContext.getBean(SongService.class)))
			.add(new RemoveSongCommandHandler(applicationContext.getBean(SongService.class)))
			.add(new QueueCommandHandler(applicationContext.getBean(AudioService.class)))
			.add(new ClearCommandHandler(applicationContext.getBean(AudioService.class)))
			.add(new ComradeCommandHandler(applicationContext.getBean(ComradeService.class), applicationContext.getBean(AudioService.class)))
			.add(new UncomradeCommandHandler(applicationContext.getBean(ComradeService.class)))
			.add(new DrinkCommandHandler(applicationContext.getBean(DrinkService.class)))
			.add(new PopPopCommandHandler(applicationContext.getBean(DrinkService.class)))
			.add(new DrinksCommandHandler(applicationContext.getBean(DrinkService.class)))

		discord.start()
    }
}
