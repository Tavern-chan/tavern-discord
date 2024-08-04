package com.tavern.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.tavern.domain.model.discord.VoiceChannelId;

public class GuildMusicManager {
	public GuildMusicManager(AudioPlayerManager manager, VoiceChannelId voiceChannelId) {
		this.player = manager.createPlayer();
		this.trackScheduler = new TrackScheduler(player);
		player.addListener(trackScheduler);
		this.voiceChannelId = voiceChannelId;
	}

	public AudioPlayerSendHandler getSendHandler() {
		return new AudioPlayerSendHandler(player);
	}

	public TrackScheduler getScheduler() {
		return trackScheduler;
	}

	public VoiceChannelId getVoiceChannelId() {
		return voiceChannelId;
	}

	public void setVoiceChannelId(VoiceChannelId voiceChannelId) {
		this.voiceChannelId = voiceChannelId;
	}

	private final AudioPlayer player;
	private final TrackScheduler trackScheduler;
	private VoiceChannelId voiceChannelId;
}
