package com.example.mp3player.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.mp3player.R;
import com.example.mp3player.models.Track;
import com.example.mp3player.models.User;
import com.example.mp3player.repositories.MusicRepository;
import com.example.mp3player.repositories.UserRepository;
import com.example.mp3player.services.MusicService;
import com.example.mp3player.viewmodels.PlayerViewModel;

public class PlayerActivity extends AppCompatActivity {
    
    public static final String EXTRA_TRACK = "extra_track";
    
    private PlayerViewModel playerViewModel;
    private MusicRepository musicRepository;
    private UserRepository userRepository;
    
    private ImageView ivAlbumArt;
    private TextView tvTrackTitle;
    private TextView tvArtist;
    private TextView tvAlbum;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvDuration;
    private ImageButton btnPrevious;
    private ImageButton btnPlayPause;
    private ImageButton btnNext;
    private ImageButton btnAddToLibrary;
    private ImageButton btnBack;
    
    private MusicService musicService;
    private boolean serviceBound = false;
    private Handler progressHandler;
    private Runnable progressRunnable;
    
    private Track currentTrack;
    private boolean isInLibrary = false;
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            
            playerViewModel.setMusicService(musicService);
            
            // If track was passed, play it
            if (currentTrack != null) {
                playerViewModel.play(currentTrack);
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            musicService = null;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        
        // Initialize repositories
        musicRepository = new MusicRepository(this);
        userRepository = new UserRepository(this);
        
        // Initialize ViewModel
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        
        // Initialize views
        initViews();
        
        // Get track from intent
        if (getIntent().hasExtra(EXTRA_TRACK)) {
            currentTrack = getIntent().getParcelableExtra(EXTRA_TRACK);
            if (currentTrack != null) {
                updateTrackInfo(currentTrack);
                checkLibraryStatus();
            }
        }
        
        // Setup listeners
        setupListeners();
        
        // Observe ViewModel
        observeViewModel();
        
        // Bind to MusicService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        // Setup progress updater
        setupProgressUpdater();
    }
    
    private void initViews() {
        ivAlbumArt = findViewById(R.id.ivAlbumArt);
        tvTrackTitle = findViewById(R.id.tvTrackTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvAlbum = findViewById(R.id.tvAlbum);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvDuration = findViewById(R.id.tvDuration);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnAddToLibrary = findViewById(R.id.btnAddToLibrary);
        btnBack = findViewById(R.id.btnBack);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnPlayPause.setOnClickListener(v -> playerViewModel.togglePlayPause());
        
        btnPrevious.setOnClickListener(v -> playerViewModel.previous());
        
        btnNext.setOnClickListener(v -> playerViewModel.next());
        
        btnAddToLibrary.setOnClickListener(v -> toggleLibrary());
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Stop progress updates while user is dragging
                if (progressHandler != null && progressRunnable != null) {
                    progressHandler.removeCallbacks(progressRunnable);
                }
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playerViewModel.seekTo(seekBar.getProgress());
                // Resume progress updates
                if (progressHandler != null && progressRunnable != null) {
                    progressHandler.post(progressRunnable);
                }
            }
        });
    }
    
    private void observeViewModel() {
        playerViewModel.getCurrentTrack().observe(this, track -> {
            if (track != null) {
                currentTrack = track;
                updateTrackInfo(track);
                checkLibraryStatus();
                updateNavigationButtons();
            }
        });
        
        playerViewModel.getIsPlaying().observe(this, isPlaying -> {
            if (isPlaying != null) {
                updatePlayPauseButton(isPlaying);
            }
        });
        
        playerViewModel.getCurrentPosition().observe(this, position -> {
            if (position != null && !seekBar.isPressed()) {
                seekBar.setProgress(position);
                tvCurrentTime.setText(formatTime(position));
            }
        });
        
        playerViewModel.getDuration().observe(this, duration -> {
            if (duration != null && duration > 0) {
                seekBar.setMax(duration);
                tvDuration.setText(formatTime(duration));
            }
        });
    }
    
    private void updateNavigationButtons() {
        // Enable/disable next and previous buttons based on queue position
        if (serviceBound && musicService != null) {
            btnNext.setEnabled(playerViewModel.hasNext());
            btnNext.setAlpha(playerViewModel.hasNext() ? 1.0f : 0.5f);
            
            btnPrevious.setEnabled(playerViewModel.hasPrevious() || musicService.getCurrentPosition() > 3000);
            btnPrevious.setAlpha((playerViewModel.hasPrevious() || musicService.getCurrentPosition() > 3000) ? 1.0f : 0.5f);
        }
    }
    
    private void updateTrackInfo(Track track) {
        tvTrackTitle.setText(track.getTitle());
        tvArtist.setText(track.getArtist());
        tvAlbum.setText(track.getAlbum() != null ? track.getAlbum() : "");
        
        // Load album artwork
        if (track.getArtworkUrl() != null && !track.getArtworkUrl().isEmpty()) {
            Glide.with(this)
                    .load(track.getArtworkUrl())
                    .placeholder(R.drawable.placeholder_album)
                    .error(R.drawable.placeholder_album)
                    .into(ivAlbumArt);
        } else {
            ivAlbumArt.setImageResource(R.drawable.placeholder_album);
        }
    }
    
    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }
    
    private void checkLibraryStatus() {
        if (currentTrack != null) {
            User user = userRepository.getCurrentUser();
            if (user != null) {
                isInLibrary = musicRepository.isInLibrary(user.getId(), currentTrack.getId());
                updateLibraryButton();
            }
        }
    }
    
    private void updateLibraryButton() {
        if (isInLibrary) {
            btnAddToLibrary.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            btnAddToLibrary.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }
    
    private void toggleLibrary() {
        User user = userRepository.getCurrentUser();
        if (user == null || currentTrack == null) {
            Toast.makeText(this, "Please log in to add tracks to library", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isInLibrary) {
            // Remove from library
            boolean success = musicRepository.removeFromLibrary(user.getId(), currentTrack.getId());
            if (success) {
                isInLibrary = false;
                updateLibraryButton();
                Toast.makeText(this, "Removed from library", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to remove from library", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Add to library
            boolean success = musicRepository.addToLibrary(user.getId(), currentTrack);
            if (success) {
                isInLibrary = true;
                updateLibraryButton();
                Toast.makeText(this, "Added to library", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add to library", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void setupProgressUpdater() {
        progressHandler = new Handler(Looper.getMainLooper());
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (serviceBound && musicService != null && musicService.isPlaying()) {
                    playerViewModel.updateProgress();
                }
                progressHandler.postDelayed(this, 1000); // Update every second
            }
        };
        progressHandler.post(progressRunnable);
    }
    
    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Stop progress updates
        if (progressHandler != null && progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
        }
        
        // Unbind service
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }
}
