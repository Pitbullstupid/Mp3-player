package com.example.mp3player.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.mp3player.MainActivity;
import com.example.mp3player.R;
import com.example.mp3player.models.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    private static final String CHANNEL_ID = "MusicPlaybackChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private MediaPlayer mediaPlayer;
    private Track currentTrack;
    private List<Track> playlist;
    private int currentIndex = 0;
    private final IBinder binder = new MusicBinder();
    
    private OnPlaybackStateChangeListener playbackStateChangeListener;
    
    public interface OnPlaybackStateChangeListener {
        void onPlaybackStateChanged(boolean isPlaying);
        void onTrackChanged(Track track);
        void onProgressChanged(int position, int duration);
    }
    
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        playlist = new ArrayList<>();
        
        mediaPlayer.setOnCompletionListener(mp -> {
            // Automatically play next track when current track completes
            if (hasNext()) {
                next();
            } else {
                // End of playlist - stop playback
                pause();
                if (playbackStateChangeListener != null) {
                    playbackStateChangeListener.onPlaybackStateChanged(false);
                }
            }
        });
        
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
            return false;
        });
        
        createNotificationChannel();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    public void setPlaybackStateChangeListener(OnPlaybackStateChangeListener listener) {
        this.playbackStateChangeListener = listener;
    }
    
    public void playTrack(Track track) {
        currentTrack = track;
        
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(track.getPreviewUrl());
            mediaPlayer.prepareAsync();
            
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                startForeground(NOTIFICATION_ID, createNotification());
                
                if (playbackStateChangeListener != null) {
                    playbackStateChangeListener.onPlaybackStateChanged(true);
                    playbackStateChangeListener.onTrackChanged(currentTrack);
                }
            });
            
        } catch (IOException e) {
            Log.e(TAG, "Error playing track", e);
        }
    }
    
    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification();
            
            if (playbackStateChangeListener != null) {
                playbackStateChangeListener.onPlaybackStateChanged(false);
            }
        }
    }
    
    public void resume() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateNotification();
            
            if (playbackStateChangeListener != null) {
                playbackStateChangeListener.onPlaybackStateChanged(true);
            }
        }
    }
    
    public void next() {
        if (playlist != null && !playlist.isEmpty()) {
            // Check if we're at the end of the playlist
            if (currentIndex < playlist.size() - 1) {
                currentIndex++;
                playTrack(playlist.get(currentIndex));
            } else {
                // At the end of playlist - stop playback
                pause();
                if (playbackStateChangeListener != null) {
                    playbackStateChangeListener.onPlaybackStateChanged(false);
                }
            }
        }
    }
    
    public void previous() {
        if (playlist != null && !playlist.isEmpty()) {
            // If more than 3 seconds into the track, restart current track
            if (getCurrentPosition() > 3000) {
                seekTo(0);
            } else if (currentIndex > 0) {
                // Go to previous track
                currentIndex--;
                playTrack(playlist.get(currentIndex));
            } else {
                // At the beginning of playlist - restart first track
                seekTo(0);
            }
        }
    }
    
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }
    
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
    
    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }
    
    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }
    
    public Track getCurrentTrack() {
        return currentTrack;
    }
    
    public void setPlaylist(List<Track> tracks, int startIndex) {
        this.playlist = new ArrayList<>(tracks);
        this.currentIndex = startIndex;
        if (!playlist.isEmpty() && startIndex >= 0 && startIndex < playlist.size()) {
            playTrack(playlist.get(currentIndex));
        }
    }
    
    public List<Track> getPlaylist() {
        return playlist != null ? new ArrayList<>(playlist) : new ArrayList<>();
    }
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    public boolean hasNext() {
        return playlist != null && !playlist.isEmpty() && currentIndex < playlist.size() - 1;
    }
    
    public boolean hasPrevious() {
        return playlist != null && !playlist.isEmpty() && currentIndex > 0;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Controls for music playback");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        String title = currentTrack != null ? currentTrack.getTitle() : "No track";
        String artist = currentTrack != null ? currentTrack.getArtist() : "";
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }
    
    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, createNotification());
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
