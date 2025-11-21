package com.example.mp3player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mp3player.models.Track;
import com.example.mp3player.services.MusicService;
import com.example.mp3player.ui.account.AccountFragment;
import com.example.mp3player.ui.home.HomeFragment;
import com.example.mp3player.ui.library.LibraryFragment;
import com.example.mp3player.ui.player.MiniPlayerView;
import com.example.mp3player.ui.search.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;
    private FrameLayout miniPlayerContainer;
    private MiniPlayerView miniPlayerView;
    
    private MusicService musicService;
    private boolean serviceBound = false;
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            
            // Setup playback state listener
            musicService.setPlaybackStateChangeListener(new MusicService.OnPlaybackStateChangeListener() {
                @Override
                public void onPlaybackStateChanged(boolean isPlaying) {
                    if (miniPlayerView != null) {
                        miniPlayerView.updatePlaybackState(isPlaying);
                    }
                }
                
                @Override
                public void onTrackChanged(Track track) {
                    if (miniPlayerView != null) {
                        miniPlayerView.updateTrackInfo(track);
                        showMiniPlayer();
                    }
                }
                
                @Override
                public void onProgressChanged(int position, int duration) {
                    // Not needed for mini player
                }
            });
            
            // Update mini player with current track if any
            Track currentTrack = musicService.getCurrentTrack();
            if (currentTrack != null) {
                miniPlayerView.updateTrackInfo(currentTrack);
                miniPlayerView.updatePlaybackState(musicService.isPlaying());
                showMiniPlayer();
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Request notification permission for Android 13+
        if (!com.example.mp3player.utils.PermissionHelper.hasNotificationPermission(this)) {
            com.example.mp3player.utils.PermissionHelper.requestNotificationPermission(this);
        }
        
        bottomNavigation = findViewById(R.id.bottomNavigation);
        miniPlayerContainer = findViewById(R.id.miniPlayerContainer);
        
        // Create and add MiniPlayerView
        miniPlayerView = new MiniPlayerView(this);
        miniPlayerContainer.addView(miniPlayerView);
        
        // Setup mini player play/pause listener
        miniPlayerView.setOnPlayPauseClickListener(() -> {
            if (serviceBound && musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                } else {
                    musicService.resume();
                }
            }
        });
        
        setupBottomNavigation();
        
        // Bind to MusicService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.navigation_search) {
                fragment = new SearchFragment();
            } else if (itemId == R.id.navigation_library) {
                fragment = new LibraryFragment();
            } else if (itemId == R.id.navigation_account) {
                fragment = new AccountFragment();
            }
            
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            
            return false;
        });
    }
    
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
    
    public void showMiniPlayer() {
        miniPlayerContainer.setVisibility(View.VISIBLE);
    }
    
    public void hideMiniPlayer() {
        miniPlayerContainer.setVisibility(View.GONE);
    }
    
    public void playTrack(Track track) {
        if (serviceBound && musicService != null) {
            musicService.playTrack(track);
            showMiniPlayer();
        }
    }
    
    public void playTracks(List<Track> tracks, int startIndex) {
        if (serviceBound && musicService != null) {
            musicService.setPlaylist(tracks, startIndex);
            showMiniPlayer();
        }
    }
    
    public MusicService getMusicService() {
        return musicService;
    }
    
    public boolean isServiceBound() {
        return serviceBound;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == com.example.mp3player.utils.PermissionHelper.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                android.widget.Toast.makeText(this, "Notification permission granted", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                android.widget.Toast.makeText(this, "Notification permission denied. You won't see playback notifications.", android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Update mini player when returning from PlayerActivity
        if (serviceBound && musicService != null) {
            Track currentTrack = musicService.getCurrentTrack();
            if (currentTrack != null) {
                miniPlayerView.updateTrackInfo(currentTrack);
                miniPlayerView.updatePlaybackState(musicService.isPlaying());
                showMiniPlayer();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }
}