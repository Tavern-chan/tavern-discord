package com.tavern.discord.audio;

import com.google.common.collect.Iterables;
import com.tavern.domain.model.TavernCommands;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.audio.SongService;
import com.tavern.domain.model.audio.SpotifyService;
import com.tavern.domain.model.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class PlayCommandHandler implements CommandHandler {
	private final SongService songService;
	private final AudioService audioService;
	private final SpotifyService spotifyService;

	public PlayCommandHandler(SongService songService, AudioService audioService) {
		this.songService = songService;
		this.audioService = audioService;
		this.spotifyService = songService.getSpotifyService();
	}

	@Override
	public Command getCommand() {
		return TavernCommands.PLAY;
	}

	@Override
	public boolean supportsUsage(CommandArgumentUsage usage) {
		return true;
	}

	@Override
	public CommandResult handle(@Nonnull MessageReceivedEvent event, CommandMessage message) {
		final String songId = Iterables.getFirst(message.getArgs(), null);

		if (songId.contains(spotifyService.URI_CHECK_STRING)) {
			spotifyService.getListOfSongsFromURL(songId).ifPresentOrElse(songList -> {
				audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager());
				songList.forEach(song -> audioService.play(event.getChannel().asTextChannel(), song.getId().toString()));
			}, () -> {
				event.getChannel().sendMessage("Could not retrieve spotify songs from " + songId).queue();;
			});
		} else {
			songService.songFromString(songId).ifPresentOrElse(song -> {
				audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager());
				audioService.play(event.getChannel().asTextChannel(), song.getUri());
			}, () -> {
				audioService.join(event.getMember().getVoiceState(), event.getGuild().getAudioManager());
				audioService.play(event.getChannel().asTextChannel(), String.join(" ", message.getArgs()));
			});
		}

		return new CommandResultBuilder().success().build();
	}

}
