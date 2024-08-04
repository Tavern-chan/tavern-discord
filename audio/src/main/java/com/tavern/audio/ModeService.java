package com.tavern.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.audio.Song;
import com.tavern.domain.model.audio.SongRepository;
import com.tavern.utilities.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ModeService {
    private final SongRepository songRegistry;

    private AudioService audioService;
    private String category;
    private AudioTrack weaveSongTrack;
    private MODE mode;
    private List<Song> categoryQueue;

    public enum MODE {
        WEAVE,
        CATEGORY,
        DEFAULT;
    }

    public ModeService(@Autowired SongRepository songRegistry) {
        this.songRegistry = songRegistry;
        mode = MODE.DEFAULT;
        category = "";
    }

    public void defaultMode() {
        this.mode = MODE.DEFAULT;
    }

    public MODE getMode() {
        return mode;
    }

    public String getCategory() {
        return category;
    }

    public void setWeave(AudioTrack track) {
        this.weaveSongTrack = track;
        this.setMode(MODE.WEAVE);
    }

    public void setCategory(String category, AudioService audioService) {
        this.audioService = audioService;
        this.category = category;
        categoryQueue = StreamSupport.stream(songRegistry.getAll().spliterator(), false)
            .filter(s -> Objects.equals(s.getCategory(), getCategory()))
            .collect(Collectors.toList());
        Collections.shuffle(categoryQueue);
        this.setMode(MODE.CATEGORY);
    }

    private void refreshCategoryQueue() {
        categoryQueue = StreamSupport.stream(songRegistry.getAll().spliterator(), false)
            .filter(s -> Objects.equals(s.getCategory(), getCategory()))
            .collect(Collectors.toList());
        Collections.shuffle(categoryQueue);
    }

    public AudioTrack getNext() {
        switch (mode) {
            case CATEGORY: {
                try {
                    return audioService.getAudioTrack(CollectionUtils.pop(categoryQueue).getUri());
                } catch (NoSuchElementException e) {
                    refreshCategoryQueue();
                    return audioService.getAudioTrack(CollectionUtils.pop(categoryQueue).getUri());
                }
            }
            case WEAVE:
                return weaveSongTrack;
            case DEFAULT:
                // fall-through
            default:
                return null;
        }
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

}
