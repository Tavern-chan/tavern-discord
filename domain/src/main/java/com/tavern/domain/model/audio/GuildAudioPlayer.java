package com.tavern.domain.model.audio;

import com.tavern.domain.model.audio.song.Song;
import com.tavern.domain.model.audio.song.SongMetadata;
import com.tavern.domain.model.discord.VoiceChannelId;

public interface GuildAudioPlayer {
    void join(VoiceChannelId channelId);
    void leave();

    void stop();
    void pause();
    void resume();
    default void skip() {
        skip(1);
    }
    void skip(int skip);
    void remove(int start, int end);
    void clear();

    SongMetadata play(Song song);
    SongMetadata playNow(Song song);
    SongMetadata nowPlaying(Song song);

    void weave(Song song);
    void playMode(String category);
    void shuffle(ShuffleOption shuffleOption);
    void repeat(RepeatOption repeatOption);

}
