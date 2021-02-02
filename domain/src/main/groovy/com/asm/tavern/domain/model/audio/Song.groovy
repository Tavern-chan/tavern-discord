package com.asm.tavern.domain.model.audio

import com.asm.tavern.domain.model.IdentifiedDomainObject

class Song extends IdentifiedDomainObject<SongId> {
	private URI uri

	Song() {
		super(null)
	}

	Song(SongId songId, URI uri) {
		super(songId)
		this.uri = uri
	}

	URI getUri() {
		uri
	}

}
