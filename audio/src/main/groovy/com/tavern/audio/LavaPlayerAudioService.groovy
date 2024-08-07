package com.tavern.audio

import com.tavern.domain.model.audio.ActiveAudioTrack
import com.tavern.domain.model.audio.AudioService
import com.tavern.domain.model.audio.AudioTrackInfo
import com.tavern.domain.model.discord.GuildId
import com.tavern.domain.model.discord.VoiceChannelId
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.lavalink.youtube.YoutubeAudioSourceManager
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.managers.AudioManager
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import java.util.stream.Collectors

class LavaPlayerAudioService implements AudioService {
	private static final XLogger logger = XLoggerFactory.getXLogger(LavaPlayerAudioService.class)
	private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager()
	private final YoutubeAudioSourceManager ytSourceManager = new YoutubeAudioSourceManager()
	private final Map<GuildId, GuildMusicManager> musicManagers = new HashMap<>()
	private final YOUTUBE_SEARCH_PREFIX = "ytsearch:"
	private final ModeService modeService

	LavaPlayerAudioService(ModeService modeService) {
		playerManager.registerSourceManager(ytSourceManager);
		AudioSourceManagers.registerRemoteSources(playerManager,
				com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);
		AudioSourceManagers.registerLocalSource(playerManager)
		this.modeService = modeService
	}

	@Override
	void join(GuildVoiceState voiceState, AudioManager audioManager) {
		if (null != voiceState && null != voiceState.getChannel()) {
			audioManager.openAudioConnection(voiceState.getChannel())
			getGuildAudioPlayer(voiceState).voiceChannelId = new VoiceChannelId(voiceState.getChannel().getId())
			getGuildAudioPlayer(voiceState).scheduler.modeService = this.modeService
		}
	}

	@Override
	void leave(Guild guild) {
		guild.getAudioManager().closeAudioConnection()
		musicManagers.get(new GuildId(guild.getId())).voiceChannelId = null
	}

	@Override
	VoiceChannelId getCurrentChannel(GuildId guildId) {
		musicManagers.get(guildId).voiceChannelId
	}

	@Override
	void play(TextChannel textChannel, URI uri) {
		GuildId guildId = new GuildId(textChannel.getGuild().getId())
		GuildMusicManager musicManager = musicManagers.get(guildId)
		musicManager.scheduler.setChannelId(textChannel)

		playerManager.loadItemOrdered(musicManager, uri.toString(), new AudioLoadResultHandler() {
			@Override
			void trackLoaded(AudioTrack track) {
				if(getNowPlaying(guildId)){
					textChannel.sendMessage("Adding to queue ${track.getInfo().title}").queue()
				}
				scheduleTrack(musicManager, track)
			}

			@Override
			void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack()

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().first()
				}

				int currentIndex = playlist.getTracks().indexOf(firstTrack)
				if (currentIndex == -1) {
					logger.warn("Could not find track index, starting from the top")
					currentIndex = 0
				}

				for (int i = currentIndex; i < playlist.tracks.size(); i++) {
					scheduleTrack(musicManager, playlist.tracks[i])
				}

				textChannel.sendMessage("Adding playlist ${playlist.name} to queue. Starting with the track ${playlist.tracks[currentIndex].info.title}").queue()
			}

			@Override
			void noMatches() {
				textChannel.sendMessage("Nothing found by ${uri}")
			}

			@Override
			void loadFailed(FriendlyException exception) {
				textChannel.sendMessage("Could not play: ${exception.getMessage()}").queue()
			}
		})
	}

	@Override
	void play(TextChannel textChannel, String searchString) {
		GuildId guildId = new GuildId(textChannel.getGuild().getId())
		GuildMusicManager musicManager = musicManagers.get(guildId)
		musicManager.scheduler.setChannelId(textChannel)


		playerManager.loadItemOrdered(musicManager, YOUTUBE_SEARCH_PREFIX + searchString, new AudioLoadResultHandler() {
			@Override
			void trackLoaded(AudioTrack track) {
				if(getNowPlaying(guildId)){
					textChannel.sendMessage("Adding to queue ${track.getInfo().title}").queue()
				}
				scheduleTrack(musicManager, track)
			}

			@Override
			void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack()

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().first()
				}
				if(getNowPlaying(guildId)){
					textChannel.sendMessage("Adding to queue ${firstTrack.getInfo().title}").queue()
				}
				scheduleTrack(musicManager, firstTrack)
			}

			@Override
			void noMatches() {
				textChannel.sendMessage("Nothing found by ${searchString}")
			}

			@Override
			void loadFailed(FriendlyException exception) {
				textChannel.sendMessage("Could not play: ${exception.getMessage()}").queue()
			}
		})
	}

	@Override
	void playNext(TextChannel textChannel, URI uri) {
		GuildId guildId = new GuildId(textChannel.getGuild().getId())
		GuildMusicManager musicManager = musicManagers.get(guildId)
		musicManager.scheduler.setChannelId(textChannel)


		playerManager.loadItemOrdered(musicManager, uri.toString(), new AudioLoadResultHandler() {
			@Override
			void trackLoaded(AudioTrack track) {
				if(!getNowPlaying(guildId)){
					musicManagers.get(guildId).getScheduler().playNext(track)
				}
				else{
					textChannel.sendMessage("Adding ${track.getInfo().title} to queue after currently playing song.").queue()
					musicManagers.get(guildId).getScheduler().playNext(track)
				}

			}

			@Override
			void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack()

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().first()
				}

				int currentIndex = playlist.getTracks().indexOf(firstTrack)
				if (currentIndex == -1) {
					logger.warn("Could not find track index, starting from the top")
					currentIndex = 0
				}


				// if nothing in queue, we just add the playlist normally else we add it in reverse order to the position this maintains playlist order while placing in queue at correct position

				if(!getNowPlaying(guildId)){
					for (int i = currentIndex; i < playlist.tracks.size(); i++) {
						scheduleTrack(musicManager, playlist.tracks[i])
					}

					textChannel.sendMessage("Adding playlist ${playlist.name} to queue. Starting with the track ${playlist.tracks[currentIndex].info.title}").queue()
				}
				else{
					// Modified from play command, if adding a playlist to play next we would want to reverse order the list when sending it to queue
					// This is because adding a playlist one song at a time to a specific position would reverse order.
					// a,b,c + playlist(1,2,3) by going a,b,c + 1; a,b,c,1 + 2; etc... =a,b,c,1,2, 3 This works for just adding to the end of the queue
					// a,b,c + playlist(1,2,3) by going 1,a,b,c; 2,1,a,b,c; etc... = 3,2,1,a,b,c But when placing at the first position would mess up the order of the list added
					for (int i = playlist.tracks.size(); i > 0; i--) {
						musicManagers.get(guildId).getScheduler().playNext(playlist.tracks[i-1])
					}

					textChannel.sendMessage("Adding playlist ${playlist.name} to queue after currently playing song. Starting with the track ${playlist.tracks[currentIndex].info.title}").queue()

				}
			}

			@Override
			void noMatches() {
				textChannel.sendMessage("Nothing found by ${uri}")
			}

			@Override
			void loadFailed(FriendlyException exception) {
				textChannel.sendMessage("Could not play: ${exception.getMessage()}").queue()
			}
		})
	}

	@Override
	void playNext(TextChannel textChannel, String searchString) {
		GuildId guildId = new GuildId(textChannel.getGuild().getId())
		GuildMusicManager musicManager = musicManagers.get(guildId)
		musicManager.scheduler.setChannelId(textChannel)

		// playNext with a search string gets a playlist back from the search, so handle it like we only want the first song.


		playerManager.loadItemOrdered(musicManager, YOUTUBE_SEARCH_PREFIX + searchString, new AudioLoadResultHandler() {
			@Override
			void trackLoaded(AudioTrack track) {
				if(!getNowPlaying(guildId)){
					scheduleTrack(musicManager, track)
				}
				else{
					textChannel.sendMessage("Adding ${track.getInfo().title} to queue after currently playing song.").queue()
					scheduleTrackNext(musicManager, track)
				}

			}

			@Override
			void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack()

				if (firstTrack == null) {
					firstTrack = playlist.getTracks().first()
				}
				if(!getNowPlaying(guildId)){
					scheduleTrack(musicManager, playlist.getTracks().first())
				}
				else{
					scheduleTrackNext(musicManager, playlist.getTracks().first())
					textChannel.sendMessage("Adding ${firstTrack.info.title} to queue after currently playing song.").queue()

				}
			}

			@Override
			void noMatches() {
				textChannel.sendMessage("Nothing found by ${searchString}")
			}

			@Override
			void loadFailed(FriendlyException exception) {
				textChannel.sendMessage("Could not play: ${exception.getMessage()}").queue()
				logger.error "Failed to play track", exception
			}
		})
	}

	private void scheduleTrack(GuildMusicManager musicManager, AudioTrack track) {
		musicManager.getScheduler().queue(track)
	}

	private void scheduleTrackNext(GuildMusicManager musicManager, AudioTrack track) {
		musicManager.getScheduler().playNext(track)
	}

	private synchronized GuildMusicManager getGuildAudioPlayer(GuildVoiceState guildVoiceState) {
		GuildId guildId = new GuildId(guildVoiceState.getGuild().getId())
		GuildMusicManager musicManager = musicManagers.get(guildId)

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager, new VoiceChannelId(guildVoiceState.getChannel().getId()))
			musicManagers.put(guildId, musicManager)
		}

		guildVoiceState.getGuild().getAudioManager().setSendingHandler(musicManager.getSendHandler())

		return musicManager
	}

	@Override
	void skip(GuildId guildId, int amount) {
		GuildMusicManager manager = musicManagers.get(guildId)
		if (null != manager) {
			manager.getScheduler().skip(amount)
		}
	}

	@Override
	void skipTime(GuildId guildId, int amount) {
		GuildMusicManager manager = musicManagers.get(guildId)
		if(null != manager) {
			manager.getScheduler().skipTime(amount)
		}
	}

	@Override
	void stop(GuildId guildId) {
		GuildMusicManager manager = musicManagers.get(guildId)
		if (null != manager) {
			manager.getScheduler().stopAndClear()
		}
	}

	@Override
	void clear(GuildId guildId) {
		GuildMusicManager manager = musicManagers.get(guildId)
		if (null != manager) {
			manager.getScheduler().clear()
		}
	}

	@Override
	void pause(GuildId guildId) {
		GuildMusicManager manager = musicManagers.get(guildId)
		if (null != manager) {
			manager.getScheduler().pause()
		}
	}

	@Override
	void unpause(GuildId guildId) {
		GuildMusicManager manager = musicManagers.get(guildId)
		if (null != manager) {
			manager.getScheduler().unpause()
		}
	}

	@Override
	List<AudioTrackInfo> getQueue(GuildId guildId) {
		musicManagers.get(guildId).getScheduler().getQueue().stream().map({track ->
			new AudioTrackInfoAdapter(track.info)
		}).collect(Collectors.toList())
	}

	@Override
	ActiveAudioTrack getNowPlaying(GuildId guildId) {
		(ActiveAudioTrack) Optional.ofNullable(musicManagers.get(guildId))
				.map(GuildMusicManager::getScheduler)
				.map(TrackScheduler::getNowPlaying)
				.map(ActiveAudioTrackAdapter::new)
				.orElse(null)
	}

	@Override
	void shuffle(GuildId guildId) {
		musicManagers.get(guildId).getScheduler().shuffle()
	}

	@Override
	boolean getIsPaused(GuildId guildId) {
		musicManagers.get(guildId).getScheduler().getIsPaused()
	}

	@Override
	AudioTrack getAudioTrack(String searchString) {
		AudioTrack audioTrack
		playerManager.loadItem(YOUTUBE_SEARCH_PREFIX + searchString, new AudioLoadResultHandler() {
			@Override
			void trackLoaded(AudioTrack track) {
				audioTrack = track
			}

			@Override
			void playlistLoaded(AudioPlaylist playlist) {
				audioTrack = playlist.getSelectedTrack()

				if (audioTrack == null) {
					audioTrack = playlist.getTracks().first()
				}

			}

			@Override
			void noMatches() {

			}

			@Override
			void loadFailed(FriendlyException exception) {

			}
		}).get()
		return audioTrack
	}

	@Override
	AudioTrack getAudioTrack(URI uri) {
		AudioTrack audioTrack
		playerManager.loadItem(uri.toString(), new AudioLoadResultHandler() {
			@Override
			void trackLoaded(AudioTrack track) {
				audioTrack = track
			}

			@Override
			void playlistLoaded(AudioPlaylist playlist) {
				audioTrack = playlist.getSelectedTrack()

				if (audioTrack == null) {
					audioTrack = playlist.getTracks().first()
				}

			}

			@Override
			void noMatches() {

			}

			@Override
			void loadFailed(FriendlyException exception) {

			}
		}).get()
		return audioTrack
	}

	@Override
	void setWeaveAudio(URI uri) {
		modeService.setWeave(getAudioTrack(uri))
	}

	@Override
	void setCategory(String category) {
		modeService.setCategory(category, this)
	}

	@Override
	void setWeaveAudio(String searchString) {
		modeService.setWeave(getAudioTrack(searchString))
	}

	@Override
	void clearPlayMode(){
		modeService.defaultMode()
	}

	@Override
	void forcePlay(TextChannel textChannel) {
		GuildId guildId = new GuildId(textChannel.getGuild().getId())
		GuildMusicManager musicManager = musicManagers.get(guildId)
		musicManager.scheduler.setChannelId(textChannel)
		musicManagers.get(guildId).getScheduler().forceNext()
	}
}
