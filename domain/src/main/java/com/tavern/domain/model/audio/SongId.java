package com.tavern.domain.model.audio;

import com.tavern.domain.model.Identifier;

public class SongId extends Identifier {
    private SongId() { }

    public SongId(String id) {
        super(id);
    }
}
