package com.asm.tavern.discord.audio

import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.audio.Song
import com.asm.tavern.domain.model.audio.SongId
import com.asm.tavern.domain.model.audio.SongRepository
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.springframework.beans.factory.annotation.Autowired
import java.util.stream.StreamSupport

class ModeService {

    private final SongRepository songRegistry
    private AudioService audioService

    private final enum MODE{
        WEAVE,
        CATEGORY,
        DEFAULT
    }

    private String category
    private AudioTrack weaveSongTrack
    MODE mode

    private List<Song> categoryQueue

    ModeService(@Autowired SongRepository songRegistry){
        this.songRegistry = songRegistry
        mode = MODE.DEFAULT
        category = ""
    }

    void defaultMode() {
        this.mode = MODE.DEFAULT
    }

    MODE getMode() {
        mode
    }

    String getCategory() {
        category
    }

    void setWeave(AudioTrack track){
        this.weaveSongTrack = track
        this.setMode(MODE.WEAVE)
    }

    void setCategory(String category, AudioService audioService) {
        this.audioService = audioService
        this.category = category
        categoryQueue = StreamSupport.stream(songRegistry.getAll().spliterator(), false)
                .filter(s -> s.category == getCategory())
                .collect()
        Collections.shuffle(categoryQueue)
        this.setMode(MODE.CATEGORY)
    }

    private void refreshCategoryQueue(){
        categoryQueue = StreamSupport.stream(songRegistry.getAll().spliterator(), false)
                .filter(s -> s.category == getCategory())
                .collect()
        Collections.shuffle(categoryQueue)
    }

    AudioTrack getNext(){
        switch(mode){
            case MODE.CATEGORY:
                {
                    try{
                        return audioService.getAudioTrack(categoryQueue.pop().uri)
                    }
                    catch(NoSuchElementException e){
                        refreshCategoryQueue()
                        return audioService.getAudioTrack(categoryQueue.pop().uri)
                    }
                }
            case MODE.WEAVE:
                return weaveSongTrack
            default:
                return null
        }
    }

}
