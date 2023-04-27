package com.asm.tavern.discord.discord.audio

import com.asm.tavern.domain.model.TavernCommands
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.audio.SongService
import com.asm.tavern.domain.model.audio.SpotifyService
import com.asm.tavern.domain.model.command.*
import com.asm.tavern.domain.model.discord.GuildId
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

import javax.annotation.Nonnull

class PlayCommandHandler implements CommandHandler {
	private final SongService songService
	private final AudioService audioService
	private final SpotifyService spotifyService

	PlayCommandHandler(SongService songService, AudioService audioService) {
		this.songService = songService
		this.audioService = audioService
		this.spotifyService = songService.getSpotifyService()
	}

	@Override
	Command getCommand() {
		TavernCommands.PLAY
	}

	@Override
	boolean supportsUsage(CommandArgumentUsage usage) {
		true
	}

	// Replaces all other handle()
	@Override
	CommandResult handle(TextChannel textChannel = null, @Nonnull Guild guild, Member member, CommandMessage message){
		String songId = message.args.first()

		String resultMessage = ""

		//Check for spotify link here so we can play multiple times for a playlist from spotify
		if(songId.contains(spotifyService.URI_CHECK_STRING)){
			spotifyService.getListOfSongsFromURL(songId).ifPresentOrElse( songList -> {
				audioService.join(member.getVoiceState(), guild.getAudioManager())
				songList.stream()
						.forEach(song -> audioService.play(textChannel, song.id.toString()))

			}, () -> {
				resultMessage = "Could not retrieve spotify songs from ${songId}"
				textChannel.sendMessage(resultMessage).queue()
			})
		}
		else{
			songService.songFromString(songId).ifPresentOrElse(song -> {
				audioService.join(member.getVoiceState(), guild.getAudioManager())
				audioService.play(textChannel, song.uri)
			}, () -> {
				// this will search the message on youtube
				audioService.join(member.getVoiceState(), guild.getAudioManager())
				audioService.play(textChannel, message.args.join(" "))
			})
		}
		new CommandResultBuilder().success().result(resultMessage).build()

	}

	@Override
	CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		String songId = message.args.first()

        String resultMessage = ""

		//Check for spotify link here so we can play multiple times for a playlist from spotify
        if(songId.contains(spotifyService.URI_CHECK_STRING)){
            spotifyService.getListOfSongsFromURL(songId).ifPresentOrElse( songList -> {
                audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager())
                songList.stream()
                        .forEach(song -> audioService.play(event.getChannel().asTextChannel(), song.id.toString()))

            }, () -> {
                resultMessage = "Could not retrieve spotify songs from ${songId}"
                event.getChannel().sendMessage(resultMessage).queue()
            })
        }
        else{
            songService.songFromString(songId).ifPresentOrElse(song -> {
                audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager())
                audioService.play(event.getChannel().asTextChannel(), song.uri)
            }, () -> {
                // this will search the message on youtube
                audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager())
                audioService.play(event.getChannel().asTextChannel(), message.args.join(" "))
            })
        }
		new CommandResultBuilder().success().result(resultMessage).build()
	}

}
