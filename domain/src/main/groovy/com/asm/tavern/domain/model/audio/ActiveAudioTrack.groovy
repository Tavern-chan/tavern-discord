package com.asm.tavern.domain.model.audio

import java.time.Duration

interface ActiveAudioTrack {

	Duration getCurrentTime()

	AudioTrackInfo getInfo()

}