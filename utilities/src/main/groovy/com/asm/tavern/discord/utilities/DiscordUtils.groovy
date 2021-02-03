package com.asm.tavern.discord.utilities

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.VoiceChannel

class DiscordUtils {

	static String escapeUrl(String url) {
		"<${url}>"
	}

	static Optional<VoiceChannel> getUsersVoiceChannel(JDA jda, String userId) {
		return jda.getVoiceChannels().stream()
				.filter(channel -> channel.getMembers().stream()
						.anyMatch(member -> member.getUser().getId() == userId)).findFirst();
	}

}
