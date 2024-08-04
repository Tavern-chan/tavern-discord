package com.tavern.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.Objects;
import java.util.Optional;

public class DiscordUtils {

	public static String escapeUrl(final String url) {
		return "<" + url + ">";
	}

	public static Optional<VoiceChannel> getUsersVoiceChannel(JDA jda, String userId) {
		return jda.getVoiceChannels().stream()
			.filter(channel -> channel.getMembers().stream()
				.anyMatch(member -> Objects.equals(member.getUser().getId(), userId))
			).findFirst();
	}

}
