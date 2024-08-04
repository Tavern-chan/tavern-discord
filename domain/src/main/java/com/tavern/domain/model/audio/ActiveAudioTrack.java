package com.tavern.domain.model.audio;

import java.time.Duration;

public interface ActiveAudioTrack {
	Duration getCurrentTime();

	AudioTrackInfo getInfo();
}
