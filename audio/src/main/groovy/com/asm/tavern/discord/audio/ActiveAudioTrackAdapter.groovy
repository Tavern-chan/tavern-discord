package com.asm.tavern.discord.audio

import com.asm.tavern.domain.model.audio.ActiveAudioTrack
import com.asm.tavern.domain.model.audio.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

import java.time.Duration

class ActiveAudioTrackAdapter implements ActiveAudioTrack {
	private final AudioTrack track
	private final AudioTrackInfo info

	ActiveAudioTrackAdapter(AudioTrack track) {
		this.track = track
		this.info = new AudioTrackInfoAdapter(track.info)
	}

	@Override
	Duration getCurrentTime() {
		Duration.ofMillis(track.position)
	}

	@Override
	AudioTrackInfo getInfo() {
		info
	}

}
