package com.example.mp3player.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp3player.R;
import com.example.mp3player.models.Track;
import com.example.mp3player.ui.adapters.TrackAdapter;
import com.example.mp3player.viewmodels.HomeViewModel;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private RecyclerView rvRecommended;
    private RecyclerView rvNewReleases;
    private TrackAdapter recommendedAdapter;
    private TrackAdapter newReleasesAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        rvRecommended = view.findViewById(R.id.rvRecommended);
        rvNewReleases = view.findViewById(R.id.rvNewReleases);
        
        setupRecyclerViews();
        observeViewModel();
        
        homeViewModel.loadRecommendations();
        homeViewModel.loadNewReleases();
        
        return view;
    }
    
    private void setupRecyclerViews() {
        // Recommended tracks
        recommendedAdapter = new TrackAdapter(new TrackAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(Track track) {
                com.example.mp3player.MainActivity activity = (com.example.mp3player.MainActivity) requireActivity();
                // Play track with full playlist queue
                java.util.List<Track> tracks = homeViewModel.getRecommendedTracks().getValue();
                if (tracks != null) {
                    int index = tracks.indexOf(track);
                    activity.playTracks(tracks, index);
                } else {
                    activity.playTrack(track);
                }
                Toast.makeText(requireContext(), "Playing: " + track.getTitle(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onMoreClick(Track track) {
                showAddToLibraryDialog(track);
            }
        });
        
        rvRecommended.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecommended.setAdapter(recommendedAdapter);
        
        // New releases
        newReleasesAdapter = new TrackAdapter(new TrackAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(Track track) {
                com.example.mp3player.MainActivity activity = (com.example.mp3player.MainActivity) requireActivity();
                // Play track with full playlist queue
                java.util.List<Track> tracks = homeViewModel.getNewReleases().getValue();
                if (tracks != null) {
                    int index = tracks.indexOf(track);
                    activity.playTracks(tracks, index);
                } else {
                    activity.playTrack(track);
                }
                Toast.makeText(requireContext(), "Playing: " + track.getTitle(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onMoreClick(Track track) {
                showAddToLibraryDialog(track);
            }
        });
        
        rvNewReleases.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNewReleases.setAdapter(newReleasesAdapter);
    }
    
    private void observeViewModel() {
        homeViewModel.getRecommendedTracks().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                recommendedAdapter.updateTracks(tracks);
            }
        });
        
        homeViewModel.getNewReleases().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                newReleasesAdapter.updateTracks(tracks);
            }
        });
        
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showAddToLibraryDialog(Track track) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(track.getTitle())
                .setMessage("Add this track to your library?")
                .setPositiveButton("Add", (dialog, which) -> {
                    com.example.mp3player.viewmodels.LibraryViewModel libraryViewModel = 
                        new ViewModelProvider(requireActivity()).get(com.example.mp3player.viewmodels.LibraryViewModel.class);
                    libraryViewModel.addToLibrary(track);
                    Toast.makeText(requireContext(), "Added to library", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
