package com.tavern.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.tavern.domain.model.audio.song.*;

public class LavaplayerSongReader implements SongReader {
    private static final String YOUTUBE_SEARCH_PREFIX = "ytsearch:";

    private final AudioPlayerManager audioPlayerManager;
    private AudioTrack audioTrack;

    public LavaplayerSongReader(AudioPlayerManager audioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager;
    }

    @Override
    public void play(SavedSong song) {
        throw new UnsupportedOperationException("Saved songs aren't supported yet.");
    }

    @Override
    public void play(YoutubeSong song) {
        this.audioPlayerManager.loadItemOrdered(audioPlayerManager.createPlayer(), song.getUrl(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }

    @Override
    public void play(YoutubeSearchSong song) {
        this.audioPlayerManager.loadItemOrdered(audioPlayerManager.createPlayer(), YOUTUBE_SEARCH_PREFIX + song.getQuery(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }
}
