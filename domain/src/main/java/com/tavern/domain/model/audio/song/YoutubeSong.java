package com.tavern.domain.model.audio.song;

public final class YoutubeSong implements Song {
    private final String url;

    public YoutubeSong(String url) {
        this.url = url;
    }

    @Override
    public void play(SongReader player) {
        player.play(this);
    }

    public String getUrl() {
        return url;
    }
}
