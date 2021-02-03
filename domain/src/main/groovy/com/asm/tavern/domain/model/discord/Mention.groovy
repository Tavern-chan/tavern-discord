package com.asm.tavern.domain.model.discord

import net.dv8tion.jda.api.entities.Member

class Mention {
	private final String id

	Mention(String id) {
		this.id = id
	}

	Mention(Member member) {
		this(member.id)
	}

	@Override
	String toString() {
		"<@${id}>"
	}

}
