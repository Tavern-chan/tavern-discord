package com.tavern.domain.model.audio;

import java.util.Optional;

public interface SongRepository {
	Iterable<Song> getAll();

	Optional<Song> get(SongId id);

	void add(Song song);

	boolean remove(SongId id);
}
