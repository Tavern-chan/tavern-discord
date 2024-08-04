package com.tavern.domain.model.audio;

import com.tavern.domain.model.IdentifiedDomainObject;

import java.net.URI;

public class Song extends IdentifiedDomainObject<SongId> {
	private URI uri;
	private String category;

	public Song() {
		super(null);
	}

	public Song(SongId songId, URI uri, String category) {
		super(songId);
		this.uri = uri;
		this.category = category;
	}

	public Song(SongId songId, URI uri) {
		this(songId, uri, "uncategorized");
	}

	public URI getUri() {
		return uri;
	}

	public String getCategory() {
		return category;
	}

}
