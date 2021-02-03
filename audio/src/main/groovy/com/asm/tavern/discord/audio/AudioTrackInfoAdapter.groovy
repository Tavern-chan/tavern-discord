package com.asm.tavern.discord.audio

import com.asm.tavern.domain.model.audio.AudioTrackInfo

import java.time.Duration

class AudioTrackInfoAdapter implements AudioTrackInfo {
	private final com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo audioTrack

	AudioTrackInfoAdapter(com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo audioTrack) {
		this.audioTrack = audioTrack
	}

	@Override
	String getTitle() {
		audioTrack.title
	}

	@Override
	String getAuthor() {
		audioTrack.author
	}

	@Override
	URL getUrl() {
		new URL(audioTrack.uri)
	}

	@Override
	Duration getDuration() {
		isStream() ? null : Duration.ofMillis(audioTrack.length)
	}

	@Override
	boolean isStream() {
		audioTrack.isStream
	}
}
