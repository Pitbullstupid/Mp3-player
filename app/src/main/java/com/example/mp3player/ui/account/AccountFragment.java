package com.example.mp3player.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mp3player.R;
import com.example.mp3player.models.User;
import com.example.mp3player.ui.auth.AuthActivity;
import com.example.mp3player.viewmodels.AuthViewModel;

public class AccountFragment extends Fragment {
    private AuthViewModel authViewModel;
    private TextView tvUsername;
    private TextView tvEmail;
    private Button btnLogout;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        
        setupUI();
        
        return view;
    }
    
    private void setupUI() {
        User currentUser = authViewModel.getCurrentUser().getValue();
        
        if (currentUser != null) {
            tvUsername.setText(currentUser.getUsername());
            tvEmail.setText(currentUser.getEmail());
        }
        
        btnLogout.setOnClickListener(v -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        authViewModel.logout();
                        
                        Intent intent = new Intent(requireContext(), AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
