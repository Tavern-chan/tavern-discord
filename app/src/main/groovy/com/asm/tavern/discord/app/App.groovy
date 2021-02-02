package com.asm.tavern.discord.app

import com.asm.tavern.discord.audio.LavaPlayerAudioService
import com.asm.tavern.discord.discord.Discord
import com.asm.tavern.discord.discord.audio.*
import com.asm.tavern.discord.discord.help.HelpHandler
import com.asm.tavern.discord.discord.roll.*
import com.asm.tavern.discord.repository.file.FileSongRepository
import com.asm.tavern.domain.model.DomainRegistry
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.audio.SongRepository
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.command.CommandHandlerRegistry
import com.asm.tavern.domain.model.comrade.ComradeService
import com.asm.tavern.domain.model.roll.RollService
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory
import org.springframework.context.support.GenericApplicationContext

class App {
	private static final XLogger logger = XLoggerFactory.getXLogger(App.class)
	// TODO: switch back after testing
	// private static final String PREFIX = '$'
	private static final String PREFIX = '#'

    static void main(String[] args) {
		String token = "NzQ2NDk1NzE1ODc3ODQ3MTIw.X0BKTw.TqlKz-i590w3meP0JtnzFe8H2UM"// TODO: revert to -> args[0]

		logger.info("Initializing application context") // probably don't need this: new ClassPathXmlApplicationContext("applicationContext.xml")
		GenericApplicationContext applicationContext = new GenericApplicationContext()
		applicationContext.registerBean(RollService.class, RollService::new)
		applicationContext.registerBean(AudioService.class, LavaPlayerAudioService::new)
		applicationContext.registerBean(ComradeService.class, ComradeService::new)
		applicationContext.registerBean(SongRepository.class, () -> new FileSongRepository(new File("E:/dev/test/songs.json"))) // TODO: Configurable storage path
		applicationContext.registerBean(SongService.class, () -> new SongService(applicationContext.getBean(SongRepository.class)))
		applicationContext.refresh()
		applicationContext.start()

		new DomainRegistry().setApplicationContext(applicationContext)

        new Discord(
			token,
			PREFIX,
			TavernCommands.getCommands(),
			new CommandHandlerRegistry()
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
		).start()
    }
}
