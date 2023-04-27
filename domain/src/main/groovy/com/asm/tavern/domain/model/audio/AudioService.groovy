package com.asm.tavern.domain.model.audio

import com.asm.tavern.domain.model.discord.GuildId
import com.asm.tavern.domain.model.discord.VoiceChannelId
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.managers.AudioManager

interface AudioService {

	void join(GuildVoiceState voiceState, AudioManager audioManager)

	void leave(Guild guild)

	VoiceChannelId getCurrentChannel(GuildId guildId)

	void play(TextChannel textChannel, URI song)

	void play(TextChannel textChannel, String searchString)

	void skip(GuildId guildId, int amount)

	void stop(GuildId guildId)

	void clear(GuildId guildId)

	void pause(GuildId guildId)

	void unpause(GuildId guildId)

	void shuffle(GuildId guildId)

	void playNext(TextChannel textChannel, URI song)

	void playNext(TextChannel textChannel, String searchString)

	void setWeaveAudio(String searchString)

	void setWeaveAudio(URI song)

	void setCategory(String category)

	void clearPlayMode()

	void forcePlay(GuildId guildId)

	List<AudioTrackInfo> getQueue(GuildId guildId)

	ActiveAudioTrack getNowPlaying(GuildId guildId)

	boolean getIsPaused(GuildId guildId)

	AudioTrack getAudioTrack(String searchString)

	AudioTrack getAudioTrack(URI uri)

}