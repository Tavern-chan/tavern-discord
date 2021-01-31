package com.asm.tavern.discord.audio

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

import java.util.stream.Collectors

class LavaPlayerAudioService implements AudioService {
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
	void play(TextChannel textChannel, URL url) {
		GuildMusicManager musicManager = musicManagers.get(new GuildId(textChannel.getGuild().getId()))

		playerManager.loadItemOrdered(musicManager, url.toString(), new AudioLoadResultHandler() {
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

				textChannel.sendMessage("Adding to queue ${firstTrack.getInfo().title} (first track of playlist ${playlist.name})").queue()

				scheduleTrack(musicManager, firstTrack)
			}

			@Override
			void noMatches() {
				textChannel.sendMessage("Nothing found by ${url}")
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
	void skip(GuildId guildId) {
		GuildMusicManager manager = musicManagers.get(guildId)
		if (null != manager) {
			manager.getScheduler().nextTrack()
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
	AudioTrackInfo getNowPlaying(GuildId guildId) {
		(AudioTrackInfo) Optional.ofNullable(musicManagers.get(guildId))
				.map(GuildMusicManager::getScheduler)
				.map(TrackScheduler::getNowPlaying)
				.map(AudioTrack::getInfo)
				.map(AudioTrackInfoAdapter::new)
				.orElse(null)
	}
}
