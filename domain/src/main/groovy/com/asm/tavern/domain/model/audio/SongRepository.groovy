package com.asm.tavern.domain.model.audio

interface SongRepository {

	Iterable<Song> getAll()

	Optional<Song> get(SongId id)

	void add(Song song)

	boolean remove(SongId id)

}