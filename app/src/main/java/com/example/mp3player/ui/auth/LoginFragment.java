package com.example.mp3player.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mp3player.R;
import com.example.mp3player.viewmodels.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {
    private AuthViewModel authViewModel;
    private TextInputLayout tilUsername;
    private TextInputEditText etUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        
        tilUsername = view.findViewById(R.id.tilUsername);
        etUsername = view.findViewById(R.id.etUsername);
        tilPassword = view.findViewById(R.id.tilPassword);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        progressBar = view.findViewById(R.id.progressBar);
        
        setupListeners();
        observeViewModel();
        
        return view;
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
    }
    
    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        
        // Clear previous errors
        tilUsername.setError(null);
        tilPassword.setError(null);
        
        // Validate input
        if (username.isEmpty()) {
            tilUsername.setError("Username or email is required");
            return;
        }
        
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            return;
        }
        
        authViewModel.login(username, password);
    }
    
    private void observeViewModel() {
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!isLoading);
        });
        
        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
