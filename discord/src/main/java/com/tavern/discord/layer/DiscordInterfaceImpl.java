package com.tavern.discord.layer;

import com.tavern.domain.model.discord.*;
import com.tavern.domain.model.guild.GuildId;
import com.tavern.domain.model.guild.config.DrinkConfig;
import com.tavern.domain.model.guild.config.GuildConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public final class DiscordInterfaceImpl implements DiscordInterface {
    private static final XLogger logger = XLoggerFactory.getXLogger(DiscordInterfaceImpl.class);
    
    private JDA _jda;

    public DiscordInterfaceImpl() {}

    void jda(JDA jda) {
        this._jda = jda;
    }

    private JDA jda() {
        if (_jda == null) {
            throw new IllegalStateException("Attempted to access JDA before it was initialized.");
        }
        return _jda;
    }

    @Override
    public GuildConfig newGuild(GuildId guildId) {
        Guild discordGuild = jda().getGuildById(guildId.getId());

        try {
            Role drinkRole = discordGuild.createRole()
                .setName("Drinking Buddies")
                .setMentionable(true)
                .setColor(0xF1C40F)
                .reason("Create Drinking Buddies role for drink commands")
                .submit().get();

            TextChannel drinkChannel = discordGuild.createTextChannel("drinking-campaign").addRolePermissionOverride(
                drinkRole.getIdLong(),
                Collections.singleton(Permission.VIEW_CHANNEL),
                Collections.emptySet()
            ).submit().get();

            return new GuildConfig(
                guildId,
                new DrinkConfig(
                    new com.tavern.domain.model.discord.Role(drinkRole.getId()),
                    new ChannelId(drinkChannel.getId())
                )
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while initializing guild config", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Failed to initialize guild config", ex);
        }
    }

    @Override
    public void joinVc(GuildId guildId, VoiceChannelId channelId) {
        logger.info("Joining VC {} in guild {}", channelId, guildId);
        Optional.ofNullable(jda().getGuildById(guildId.getId())).ifPresent(g ->
            Optional.ofNullable(jda().getVoiceChannelById(channelId.getId())).ifPresent(vc ->
                g.getAudioManager().openAudioConnection(vc)
            )
        );
    }

    @Override
    public void leaveVc(GuildId guildId) {
        logger.info("Leaving VC in guild {}", guildId);
        Optional.ofNullable(jda().getGuildById(guildId.getId()))
            .ifPresent(g -> g.getAudioManager().closeAudioConnection());

    }
}
