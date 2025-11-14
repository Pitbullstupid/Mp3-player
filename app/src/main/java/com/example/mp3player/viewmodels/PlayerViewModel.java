package com.example.mp3player.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mp3player.models.Track;
import com.example.mp3player.services.MusicService;

public class PlayerViewModel extends ViewModel {
    private MusicService musicService;
    
    private final MutableLiveData<Track> currentTrack = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> duration = new MutableLiveData<>(0);
    
    public void setMusicService(MusicService service) {
        this.musicService = service;
        
        if (service != null) {
            service.setPlaybackStateChangeListener(new MusicService.OnPlaybackStateChangeListener() {
                @Override
                public void onPlaybackStateChanged(boolean playing) {
                    isPlaying.postValue(playing);
                }
                
                @Override
                public void onTrackChanged(Track track) {
                    currentTrack.postValue(track);
                    if (musicService != null) {
                        duration.postValue(musicService.getDuration());
                    }
                }
                
                @Override
                public void onProgressChanged(int position, int dur) {
                    currentPosition.postValue(position);
                    duration.postValue(dur);
                }
            });
            
            // Initialize with current state
            Track track = service.getCurrentTrack();
            if (track != null) {
                currentTrack.setValue(track);
                isPlaying.setValue(service.isPlaying());
                duration.setValue(service.getDuration());
                currentPosition.setValue(service.getCurrentPosition());
            }
        }
    }
    
    public void play(Track track) {
        if (musicService != null) {
            musicService.playTrack(track);
        }
    }
    
    public void pause() {
        if (musicService != null) {
            musicService.pause();
        }
    }
    
    public void resume() {
        if (musicService != null) {
            musicService.resume();
        }
    }
    
    public void togglePlayPause() {
        if (musicService != null) {
            if (musicService.isPlaying()) {
                pause();
            } else {
                resume();
            }
        }
    }
    
    public void next() {
        if (musicService != null) {
            musicService.next();
        }
    }
    
    public void previous() {
        if (musicService != null) {
            musicService.previous();
        }
    }
    
    public void seekTo(int position) {
        if (musicService != null) {
            musicService.seekTo(position);
            currentPosition.setValue(position);
        }
    }
    
    public void updateProgress() {
        if (musicService != null) {
            currentPosition.setValue(musicService.getCurrentPosition());
            if (duration.getValue() == null || duration.getValue() == 0) {
                duration.setValue(musicService.getDuration());
            }
        }
    }
    
    public LiveData<Track> getCurrentTrack() {
        return currentTrack;
    }
    
    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }
    
    public LiveData<Integer> getCurrentPosition() {
        return currentPosition;
    }
    
    public LiveData<Integer> getDuration() {
        return duration;
    }
    
    public boolean hasNext() {
        return musicService != null && musicService.hasNext();
    }
    
    public boolean hasPrevious() {
        return musicService != null && musicService.hasPrevious();
    }
}
