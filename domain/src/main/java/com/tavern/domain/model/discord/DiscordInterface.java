package com.tavern.domain.model.discord;

import com.tavern.domain.model.guild.GuildId;
import com.tavern.domain.model.guild.config.GuildConfig;

public interface DiscordInterface {
    GuildConfig newGuild(GuildId guildId);
    void joinVc(GuildId guildId, VoiceChannelId channelId);
    void leaveVc(GuildId guildId);
}
