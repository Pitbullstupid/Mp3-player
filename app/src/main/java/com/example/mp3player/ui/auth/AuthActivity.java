package com.example.mp3player.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mp3player.R;
import com.example.mp3player.viewmodels.AuthViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Check if user is already logged in
        if (authViewModel.isLoggedIn()) {
            navigateToMain();
            return;
        }
        
        setContentView(R.layout.activity_auth);
        
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        
        setupViewPager();
        observeAuthState();
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(this, com.example.mp3player.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void setupViewPager() {
        AuthPagerAdapter adapter = new AuthPagerAdapter(this);
        viewPager.setAdapter(adapter);
        
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Login" : "Register");
        }).attach();
    }
    
    private void observeAuthState() {
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                navigateToMain();
            }
        });
    }
}
