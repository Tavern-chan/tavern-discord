package com.tavern.domain.model.discord;

import com.tavern.domain.model.Identifier;

public class UserId extends Identifier {
    private UserId() { }

    public UserId(String id) {
        super(id);
    }
}
