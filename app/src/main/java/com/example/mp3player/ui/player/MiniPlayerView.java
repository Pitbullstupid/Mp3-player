package com.example.mp3player.ui.player;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.mp3player.R;
import com.example.mp3player.models.Track;

public class MiniPlayerView extends ConstraintLayout {
    
    private ImageView ivMiniAlbumArt;
    private TextView tvMiniTrackTitle;
    private TextView tvMiniArtist;
    private ImageButton btnMiniPlayPause;
    
    private Track currentTrack;
    private boolean isPlaying = false;
    private OnPlayPauseClickListener playPauseListener;
    
    public interface OnPlayPauseClickListener {
        void onPlayPauseClick();
    }
    
    public MiniPlayerView(Context context) {
        super(context);
        init(context);
    }
    
    public MiniPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public MiniPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_mini_player, this, true);
        
        ivMiniAlbumArt = findViewById(R.id.ivMiniAlbumArt);
        tvMiniTrackTitle = findViewById(R.id.tvMiniTrackTitle);
        tvMiniArtist = findViewById(R.id.tvMiniArtist);
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause);
        
        // Set click listener for play/pause button
        btnMiniPlayPause.setOnClickListener(v -> {
            if (playPauseListener != null) {
                playPauseListener.onPlayPauseClick();
            }
        });
        
        // Set click listener for the entire view to open PlayerActivity
        setOnClickListener(v -> {
            if (currentTrack != null) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_TRACK, currentTrack);
                context.startActivity(intent);
            }
        });
    }
    
    /**
     * Update the track information displayed in the mini player
     * @param track The track to display
     */
    public void updateTrackInfo(Track track) {
        this.currentTrack = track;
        
        if (track != null) {
            tvMiniTrackTitle.setText(track.getTitle());
            tvMiniArtist.setText(track.getArtist());
            
            // Load album artwork
            if (track.getArtworkUrl() != null && !track.getArtworkUrl().isEmpty()) {
                Glide.with(getContext())
                        .load(track.getArtworkUrl())
                        .placeholder(R.drawable.placeholder_album)
                        .error(R.drawable.placeholder_album)
                        .into(ivMiniAlbumArt);
            } else {
                ivMiniAlbumArt.setImageResource(R.drawable.placeholder_album);
            }
            
            // Show the mini player
            setVisibility(VISIBLE);
        } else {
            // Hide the mini player if no track
            setVisibility(GONE);
        }
    }
    
    /**
     * Update the playback state (playing or paused)
     * @param playing true if playing, false if paused
     */
    public void updatePlaybackState(boolean playing) {
        this.isPlaying = playing;
        
        if (playing) {
            btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }
    
    /**
     * Set the listener for play/pause button clicks
     * @param listener The listener to set
     */
    public void setOnPlayPauseClickListener(OnPlayPauseClickListener listener) {
        this.playPauseListener = listener;
    }
    
    /**
     * Get the current track
     * @return The current track
     */
    public Track getCurrentTrack() {
        return currentTrack;
    }
    
    /**
     * Check if currently playing
     * @return true if playing, false otherwise
     */
    public boolean isPlaying() {
        return isPlaying;
    }
}
