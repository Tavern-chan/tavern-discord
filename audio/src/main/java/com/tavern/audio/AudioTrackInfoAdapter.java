package com.tavern.audio;

import com.tavern.domain.model.audio.AudioTrackInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class AudioTrackInfoAdapter implements AudioTrackInfo {
	public AudioTrackInfoAdapter(com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo audioTrack) {
		this.audioTrack = audioTrack;
	}

	@Override
	public String getTitle() {
		return audioTrack.title;
	}

	@Override
	public String getAuthor() {
		return audioTrack.author;
	}

	@Override
	public URL getUrl() {
		try {
			return new URL(audioTrack.uri);
		} catch (MalformedURLException ex) {
			throw new IllegalArgumentException("Malformed URL: " + audioTrack.uri);
		}
	}

	@Override
	public Duration getDuration() {
		return isStream() ? null : Duration.ofMillis(audioTrack.length);
	}

	@Override
	public boolean isStream() {
		return audioTrack.isStream;
	}

	private final com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo audioTrack;
}
