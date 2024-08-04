package com.tavern.domain.model.discord;

import com.tavern.domain.model.Identifier;

public class GuildId extends Identifier {
    private GuildId() { }

    public GuildId(String id) {
        super(id);
    }
}
