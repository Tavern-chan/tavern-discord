package com.asm.tavern.discord.audio

import com.asm.tavern.domain.model.audio.ActiveAudioTrack
import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.audio.AudioTrackInfo
import com.asm.tavern.domain.model.discord.GuildId
import com.asm.tavern.domain.model.discord.VoiceChannelId
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.managers.AudioManager
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

import java.util.stream.Collectors

class LavaPlayerAudioService implements AudioService {
	private static final XLogger logger = XLoggerFactory.getXLogger(LavaPlayerAudioService.class)
	private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager()
	private final Map<GuildId, GuildMusicManager> musicManagers = new HashMap<>()

	LavaPlayerAudioService() {
		AudioSourceManagers.registerRemoteSources(playerManager)
		AudioSourceManagers.registerLocalSource(playerManager)
	}

	@Override
	void join(GuildVoiceState voiceState, AudioManager audioManager) {
		if (null != voiceState && null != voiceState.getChannel()) {
			audioManager.openAudioConnection(voiceState.getChannel())
			getGuildAudioPlayer(voiceState).voiceChannelId = new VoiceChannelId(voiceState.getChannel().getId())
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
		GuildMusicManager musicManager = musicManagers.get(new GuildId(textChannel.getGuild().getId()))

		playerManager.loadItemOrdered(musicManager, uri.toString(), new AudioLoadResultHandler() {
			@Override
			void trackLoaded(AudioTrack track) {
				textChannel.sendMessage("Adding to queue ${track.getInfo().title}").queue()

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

	private void scheduleTrack(GuildMusicManager musicManager, AudioTrack track) {
		musicManager.getScheduler().queue(track)
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
}
