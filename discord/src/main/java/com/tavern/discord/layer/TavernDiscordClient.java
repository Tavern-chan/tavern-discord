package com.tavern.discord.layer;

import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tavern.discord.layer.command.slash.SlashCommandListener;
import com.tavern.discord.layer.command.slash.TavernSlashCommandFactory;
import com.tavern.domain.model.discord.DiscordInterface;
import com.tavern.domain.model.guild.GuildId;
import com.tavern.domain.model.guild.config.GuildConfigRepository;
import com.tavern.utilities.convert.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

public final class TavernDiscordClient implements AutoCloseable {
    private static final XLogger logger = XLoggerFactory.getXLogger(TavernDiscordClient.class);

    private final JDA jda;
    private final DiscordInterfaceImpl discordInterface;
    private final String defaultCommandPrefix;
    private final Injectables injectables;
    private final TavernSlashCommandCache slashCommandCache;
    private final TypeConverterRegistry typeConverter;

    private TavernDiscordClient(TavernDiscordClient.Builder builder) {
        this.discordInterface = builder.discordInterface;
        this.defaultCommandPrefix = builder.defaultCommandPrefix;
        this.injectables = Objects.requireNonNullElseGet(builder.injectables, () -> new Injectables.Builder().build());

        // Internal type conversions
        TypeConverterRegistry typeConverter = TypeConverterRegistries.ofRegistries(
            TypeConverterRegistries.ofConvertersBuilder()
                .add(TypeConverter.of(OptionMapping.class, String.class, OptionMapping::getAsString))
                .add(TypeConverter.of(OptionMapping.class, Long.class, OptionMapping::getAsLong))
                .add(TypeConverter.of(OptionMapping.class, Long.TYPE, OptionMapping::getAsLong))
                .add(TypeConverter.of(OptionMapping.class, Integer.class, OptionMapping::getAsInt))
                .add(TypeConverter.of(OptionMapping.class, Integer.TYPE, OptionMapping::getAsInt))
                .add(TypeConverter.of(OptionMapping.class, Double.class, OptionMapping::getAsDouble))
                .add(TypeConverter.of(OptionMapping.class, Double.TYPE, OptionMapping::getAsDouble))
                .add(TypeConverter.of(OptionMapping.class, Boolean.class, OptionMapping::getAsBoolean))
                .add(TypeConverter.of(OptionMapping.class, Boolean.TYPE, OptionMapping::getAsBoolean))
                .add(TypeConverter.of(OptionMapping.class, Message.Attachment.class, OptionMapping::getAsAttachment))
                .add(TypeConverter.of(OptionMapping.class, GuildChannelUnion.class, OptionMapping::getAsChannel))
                .add(TypeConverter.of(OptionMapping.class, Member.class, OptionMapping::getAsMember))
                .add(TypeConverter.of(OptionMapping.class, IMentionable.class, OptionMapping::getAsMentionable))
                .add(TypeConverter.of(OptionMapping.class, Role.class, OptionMapping::getAsRole))
                .add(TypeConverter.of(OptionMapping.class, User.class, OptionMapping::getAsUser))
                .add(TypeConverter.of(OptionMapping.class, TextChannel.class, option -> {
                    if (option.getChannelType() != ChannelType.TEXT) {
                        throw new RuntimeException("Channel " + option.getAsChannel().getAsMention() + " is not a text channel");
                    }
                    return option.getAsChannel().asTextChannel();
                }))
                .add(TypeConverter.of(OptionMapping.class, VoiceChannel.class, option -> {
                    if (option.getChannelType() != ChannelType.VOICE) {
                        throw new RuntimeException("Channel " + option.getAsChannel().getAsMention() + " is not a voice channel");
                    }
                    return option.getAsChannel().asVoiceChannel();
                }))
                .build(),
            TypeConverterRegistries.defaultRegistry()
        );
        if (null != builder.typeConverter) {
            typeConverter = TypeConverterRegistries.ofRegistries(
                builder.typeConverter,
                typeConverter
            );
        }
        this.typeConverter = typeConverter;

        this.slashCommandCache = new TavernSlashCommandCache(new TavernSlashCommandFactory(), builder.slashCommandListeners);

        jda = JDABuilder
            .create(builder.token, Arrays.asList(
                SCHEDULED_EVENTS,
                MESSAGE_CONTENT,
                GUILD_MESSAGE_REACTIONS,
                GUILD_MESSAGES,
                GUILD_PRESENCES,
                GUILD_VOICE_STATES,
                GUILD_MEMBERS
            ))
            .setActivity(Activity.customStatus("At the BAR"))
            .setEventPool(Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("JDA Thread %d").build()))
            .setAudioModuleConfig(new AudioModuleConfig().withDaveSessionFactory(
                 new JDaveSessionFactory()
            ))
            .disableCache(CacheFlag.EMOJI, CacheFlag.STICKER)
            .addEventListeners(new SlashCommandListenerAdapter(injectables, typeConverter, slashCommandCache))
            .build();
        this.discordInterface.jda(jda);
    }

    public void initializeGuilds(GuildConfigRepository repository, Set<GuildId> knownGuilds) {
        jda.getGuilds().stream()
            .map(Guild::getId)
            .map(GuildId::new)
            .filter(Predicate.not(knownGuilds::contains))
            .forEach(guild -> repository.save(discordInterface.newGuild(guild)));
    }

    public boolean awaitReady() throws InterruptedException {
        JDA.Status[] failOn = { JDA.Status.FAILED_TO_LOGIN, JDA.Status.SHUTTING_DOWN, JDA.Status.DISCONNECTED };
        jda.awaitStatus(JDA.Status.CONNECTED,
            failOn
        );

        JDA.Status status = jda.getStatus();
        if (JDA.Status.CONNECTED == status) {
            this.slashCommandCache.postCommands(jda);
            return true;
        } else {
            logger.error("Failed to connect to Discord API. Status: {}", status);
            return false;
        }
    }

    public DiscordInterface getInterface() {
        return discordInterface;
    }

    @Override
    public void close() {
        jda.shutdown();
    }

    public static Builder builder(DiscordInterfaceImpl discordInterface, String token) {
        return new Builder(discordInterface, token);
    }

    public static class Builder {
        private final DiscordInterfaceImpl discordInterface;
        private final String token;
        private final List<Class<? extends SlashCommandListener>> slashCommandListeners = new ArrayList<>();
        private String defaultCommandPrefix = "$";
        private Injectables injectables = null;
        private TypeConverterRegistry typeConverter = null;

        public Builder(DiscordInterfaceImpl discordInterface, String token) {
            this.discordInterface = discordInterface;
            this.token = token;
        }

        public Builder commandPrefix(String prefix) {
            this.defaultCommandPrefix = prefix;
            return this;
        }

        public Builder listener(Class<? extends SlashCommandListener> listener) {
            slashCommandListeners.add(listener);
            return this;
        }

        public Builder listeners(List<Class<? extends SlashCommandListener>> listeners) {
            slashCommandListeners.addAll(listeners);
            return this;
        }

        public Builder injectables(Injectables injectables) {
            this.injectables = injectables;
            return this;
        }

        public Builder typeConverter(TypeConverterRegistry typeConverter) {
            this.typeConverter = typeConverter;
            return this;
        }

        public TavernDiscordClient build() {
            return new TavernDiscordClient(this);
        }

    }
}
