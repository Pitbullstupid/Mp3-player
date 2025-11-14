package com.example.mp3player.models;

public class PlaybackState {
    private Track currentTrack;
    private boolean isPlaying;
    private int currentPosition;
    private int duration;

    public PlaybackState() {
    }

    public PlaybackState(Track currentTrack, boolean isPlaying, int currentPosition, int duration) {
        this.currentTrack = currentTrack;
        this.isPlaying = isPlaying;
        this.currentPosition = currentPosition;
        this.duration = duration;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
