package com.asm.tavern.domain.model.audio

import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory
import org.springframework.beans.factory.annotation.Autowired

class SongService {
	private static final XLogger logger = XLoggerFactory.getXLogger(SongService.class)

	private final SongRepository songRegistry

	SongService(@Autowired SongRepository songRegistry) {
		this.songRegistry = songRegistry
	}

	SongRepository getSongRegistry() {
		songRegistry
	}

	Song register(SongId songId, URI songUrl, String category="uncategorized") {
		logger.info("Adding new song ${songId.id} : ${songUrl}")
		Song song = new Song(songId, songUrl, category)
		songRegistry.add(song)
		song
	}

	boolean remove(SongId songId) {
		logger.info("Removing song ${songId.id}")
		songRegistry.remove(songId)
	}

	Optional<Song> songFromString(String song) {
		try {
			// Check if valid URL
			return Optional.of(new Song(null, new URL(song).toURI()))
		} catch (MalformedURLException | URISyntaxException ex) {
			logger.debug("The song ${song} is not a uri, attempt lookup")
		}
		songRegistry.get(new SongId(song))
	}

}