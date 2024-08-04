package com.tavern.domain.model.discord;

import com.tavern.domain.model.Identifier;

public class VoiceChannelId extends Identifier {
    private VoiceChannelId() { }

    public VoiceChannelId(String id) {
        super(id);
    }
}
