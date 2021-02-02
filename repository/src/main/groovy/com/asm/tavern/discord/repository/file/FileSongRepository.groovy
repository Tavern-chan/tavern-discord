package com.asm.tavern.discord.repository.file

import com.asm.tavern.domain.model.audio.Song
import com.asm.tavern.domain.model.audio.SongId
import com.asm.tavern.domain.model.audio.SongRepository
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.Synchronized
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory

class FileSongRepository implements SongRepository {
	private static final XLogger logger = XLoggerFactory.getXLogger(FileSongRepository.class)

	private final File data
	private final Map<SongId, Song> songs
	private final ObjectMapper objectMapper

	FileSongRepository(File data) throws IOException {
		this.data = data
		this.objectMapper = new ObjectMapper()
		this.songs = new HashMap<>()
		if (data.exists()) {
			for (Song song: (List<Song>) objectMapper.readValue(data, objectMapper.getTypeFactory().constructCollectionType(List.class, Song.class))) {
				songs.put(song.id, song)
			}
		}
	}

	@Override
	Iterable<Song> getAll() {
		songs.values()
	}

	@Override
	Optional<Song> get(SongId id) {
		Optional.ofNullable(songs.get(id))
	}

	@Override
	@Synchronized
	void add(Song song) {
		this.songs.put(song.getId(), song)
		persist()
	}

	@Override
	@Synchronized
	boolean remove(SongId id) {
		if (this.songs.containsKey(id)) {
			this.songs.remove(id)
			persist()
			return true
		}
		false
	}

	@Synchronized
	private void persist() {
		logger.debug("Persisting to disk")
		try (FileWriter fileWriter = new FileWriter(data)) {
			String data = objectMapper.writeValueAsString(songs.values())
			fileWriter.write(data)
		} catch (IOException ex) {
			logger.error("Failed to write song data to disk {}", ex.getMessage())
			logger.catching(ex)
		}
	}

}
