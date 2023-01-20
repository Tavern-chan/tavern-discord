package com.asm.tavern.domain.model.audio

import com.asm.tavern.domain.model.IdentifiedDomainObject

class Song extends IdentifiedDomainObject<SongId> {
	private URI uri
	private String category

	Song() {
		super(null)
	}

	Song(SongId songId, URI uri, String category="uncategorized") {
		super(songId)
		this.uri = uri
		this.category = category
	}

	URI getUri() {
		uri
	}

	String getCategory() {
		category
	}

}
