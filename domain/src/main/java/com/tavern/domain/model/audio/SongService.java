package com.tavern.domain.model.audio;

import com.tavern.utilities.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class SongService {
	private static final XLogger logger = XLoggerFactory.getXLogger(SongService.class);
	private static final Logger log = LoggerFactory.getLogger(SongService.class);

	private final SongRepository songRegistry;
	private final SpotifyService spotifyService;

	public SongService(@Autowired SongRepository songRegistry, @Autowired SpotifyService spotifyService) {
		this.songRegistry = songRegistry;
		this.spotifyService = spotifyService;
	}

	public SongRepository getSongRegistry() {
		return songRegistry;
	}

	public SpotifyService getSpotifyService() {
		return spotifyService;
	}

	public Song register(SongId songId, URI songUrl) {
		return register(songId, songUrl, null);
	}

	public Song register(SongId songId, URI songUrl, String category) {
		if (StringUtils.isNullOrBlank(category)) {
			category = "uncategorized";
		}

		logger.info("Adding new song {} : {}", songId.getId(), songUrl);
		Song song = new Song(songId, songUrl, category);
		songRegistry.add(song);
		return song;
	}

	public boolean remove(SongId songId) {
		logger.info("Removing song {}", songId.getId());
		return songRegistry.remove(songId);
	}

	public Optional<Song> songFromString(String song) {
		try {
			return Optional.of(new Song(null, new URL(song).toURI()));
		} catch (MalformedURLException | URISyntaxException ex) {
			logger.debug("The song {} is not a uri, attempt lookup", song);
			logger.catching(ex);
		}
		return songRegistry.get(new SongId(song));
	}

}