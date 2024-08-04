package com.tavern.domain.model.audio;

import java.net.URL;
import java.time.Duration;

public interface AudioTrackInfo {
	String getTitle();

	String getAuthor();

	URL getUrl();

	/**
	 * @return the duration of the song, null if a stream
	 */
	Duration getDuration();

	boolean isStream();
}
