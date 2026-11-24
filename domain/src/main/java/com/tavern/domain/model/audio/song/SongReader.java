package com.tavern.domain.model.audio.song;

public interface SongReader {
    void play(SavedSong song);
    void play(YoutubeSong song);
    void play(YoutubeSearchSong song);
}
