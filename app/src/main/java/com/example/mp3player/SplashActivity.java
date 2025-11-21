package com.example.mp3player;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mp3player.repositories.UserRepository;
import com.example.mp3player.ui.auth.AuthActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 1000; // 1 second
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check session and navigate
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            UserRepository userRepository = new UserRepository(this);
            
            Intent intent;
            if (userRepository.isLoggedIn()) {
                intent = new Intent(this, MainActivity.class);
            } else {
                intent = new Intent(this, AuthActivity.class);
            }
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
