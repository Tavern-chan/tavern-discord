package com.tavern.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.tavern.domain.model.audio.*;
import com.tavern.domain.model.audio.song.*;
import com.tavern.domain.model.discord.DiscordInterface;
import com.tavern.domain.model.discord.VoiceChannelId;
import com.tavern.domain.model.guild.GuildId;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class GuildLavaPlayer implements GuildAudioPlayer {
    private static final XLogger logger = XLoggerFactory.getXLogger(GuildLavaPlayer.class);

    private final DiscordInterface discordInterface;
    private final GuildId guildId;
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final YoutubeAudioSourceManager ytSourceManager = new YoutubeAudioSourceManager();

    public GuildLavaPlayer(DiscordInterface discordInterface, GuildId guildId) {
        this.discordInterface = discordInterface;
        this.guildId = guildId;
        audioPlayerManager.registerSourceManager(ytSourceManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager, YoutubeAudioSourceManager.class);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    @Override
    public void join(VoiceChannelId channelId) {
        discordInterface.joinVc(this.guildId, channelId);
    }

    @Override
    public void leave() {
        discordInterface.leaveVc(this.guildId);
    }

    @Override
    public void stop() {
        clear();
        skip(0);
        leave();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void skip(int skip) {

    }

    @Override
    public void remove(int start, int end) {

    }

    @Override
    public void clear() {

    }

    @Override
    public SongMetadata play(Song song) {

        return null;
    }

    @Override
    public SongMetadata playNow(Song song) {
        return null;
    }

    @Override
    public SongMetadata nowPlaying(Song song) {
        return null;
    }

    @Override
    public void weave(Song song) {

    }

    @Override
    public void playMode(String category) {

    }

    @Override
    public void shuffle(ShuffleOption shuffleOption) {

    }

    @Override
    public void repeat(RepeatOption repeatOption) {

    }
}
