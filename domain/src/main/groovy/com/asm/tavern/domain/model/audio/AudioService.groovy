package com.asm.tavern.domain.model.audio

import com.asm.tavern.domain.model.discord.GuildId
import com.asm.tavern.domain.model.discord.VoiceChannelId
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.managers.AudioManager

interface AudioService {

	void join(GuildVoiceState voiceState, AudioManager audioManager)

	void leave(Guild guild)

	VoiceChannelId getCurrentChannel(GuildId guildId)

	void play(TextChannel textChannel, URL url)

	void skip(GuildId guildId)

	void stop(GuildId guildId)

	void pause(GuildId guildId)

	void unpause(GuildId guildId)

	List<AudioTrackInfo> getQueue(GuildId guildId)

	AudioTrackInfo getNowPlaying(GuildId guildId)

}