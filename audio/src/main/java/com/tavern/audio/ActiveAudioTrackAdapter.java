package com.tavern.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.tavern.domain.model.audio.ActiveAudioTrack;
import com.tavern.domain.model.audio.AudioTrackInfo;

import java.time.Duration;

public class ActiveAudioTrackAdapter implements ActiveAudioTrack {
	public ActiveAudioTrackAdapter(AudioTrack track) {
		this.track = track;
		this.info = new AudioTrackInfoAdapter(track.getInfo());
	}

	@Override
	public Duration getCurrentTime() {
		return Duration.ofMillis(track.getPosition());
	}

	@Override
	public AudioTrackInfo getInfo() {
		return info;
	}

	private final AudioTrack track;
	private final AudioTrackInfo info;
}
