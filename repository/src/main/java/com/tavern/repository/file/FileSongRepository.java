package com.tavern.repository.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavern.domain.model.audio.Song;
import com.tavern.domain.model.audio.SongId;
import com.tavern.domain.model.audio.SongRepository;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FileSongRepository implements SongRepository {
	private static final XLogger logger = XLoggerFactory.getXLogger(FileSongRepository.class);

	private final File data;
	private final Map<SongId, Song> songs;
	private final ObjectMapper objectMapper;

	public FileSongRepository(File data) throws IOException {
		this.data = data;
		this.objectMapper = new ObjectMapper();
		this.songs = new HashMap<>();
		if (data.exists()) {
			for (Song song : (List<Song>) objectMapper.readValue(data, objectMapper.getTypeFactory().constructCollectionType(List.class, Song.class))) {
				songs.put(song.getId(), song);
			}
		}
	}

	@Override
	public Iterable<Song> getAll() {
		return songs.values();
	}

	@Override
	public Optional<Song> get(SongId id) {
		return Optional.ofNullable(songs.get(id));
	}

	@Override
	public void add(Song song) {
		this.songs.put(song.getId(), song);
		persist();
	}

	@Override
	public boolean remove(SongId id) {
		if (this.songs.containsKey(id)) {
			this.songs.remove(id);
			persist();
			return true;
		}
		return false;
	}

	private void persist() {
		logger.debug("Persisting to disk");
		try (FileWriter fileWriter = new FileWriter(data)) {
			String data = objectMapper.writeValueAsString(songs.values());
			fileWriter.write(data);
		} catch (IOException ex) {
			logger.error("Failed to write song data to disk {}", ex.getMessage());
			logger.catching(ex);
		}
	}

}
