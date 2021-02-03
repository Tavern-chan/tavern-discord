package com.asm.tavern.domain.model.audio

import java.time.Duration

interface AudioTrackInfo {

	String getTitle()

	String getAuthor()

	URL getUrl()

	/**
	 *
	 * @return the duration of the song, null if a stream
	 */
	Duration getDuration()

	boolean isStream()

}