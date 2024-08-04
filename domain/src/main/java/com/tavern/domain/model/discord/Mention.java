package com.tavern.domain.model.discord;

import net.dv8tion.jda.api.entities.Member;

public class Mention {
	private final String id;

	public Mention(String id) {
		this.id = id;
	}

	public Mention(Member member) {
		this(member.getId());
	}

	@Override
	public String toString() {
		return "<@" + id + ">";
	}
}
