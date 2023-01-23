package com.asm.tavern.discord.discord.audio

import com.asm.tavern.discord.utilities.DiscordUtils
import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.Song
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.command.*
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

import javax.annotation.Nonnull
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.StreamSupport

class SongsCommandHandler implements CommandHandler {
	private final SongService songService

	SongsCommandHandler(SongService songService) {
		this.songService = songService
	}

	@Override
	Command getCommand() {
		TavernCommands.SONGS
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		TavernCommands.SongsUsages.DEFAULT == usage
	}

	@Override
	CommandResult handle(@Nonnull GuildMessageReceivedEvent event, CommandMessage message) {
		int chunkSize = 10
		String currentCategory = ""

		// Function for pushing message so message can be pushed when max chunk size to avoid discord max message length. Also empty builder when pushed
		Function<StringBuilder, String> pushMessage = (StringBuilder builder) -> {
			event.getChannel().sendMessage(new MessageEmbed(null, "${currentCategory} Songs", builder.toString(), null, null, 0xFF0000, null, null, null, null, null,null, null)).queue()
			builder.setLength(0)
		}

        //Only needs to be ran once on upgrade, then can be removed.
		/**
        for(song in songService.getSongRegistry().getAll()){
            if(song.category == null)
				// if song category is missing, reregister the song which will autofill category with uncategorized fixing nulls
                songService.register(song.id, song.uri)
        }
	 	**/


		StreamSupport.stream(songService.getSongRegistry().getAll().spliterator(), false)
				.sorted(Comparator.comparing((Song song) -> song.id.id))
				.collect(Collectors.groupingBy(Song::getCategory))
				.values()
				.forEach({ songs ->
					int count = 0
					StringBuilder builder = new StringBuilder()
					songs.forEach({ song ->
						currentCategory = song.category
						builder.append("[${song.id}](${DiscordUtils.escapeUrl(song.uri.toString())})\n")
						if(++count == chunkSize){
							pushMessage(builder)
						}
					})
					pushMessage(builder)
				})
		new CommandResultBuilder().success().build()
	}

}
