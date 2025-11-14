package com.example.mp3player.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp3player.R;
import com.example.mp3player.models.Track;
import com.example.mp3player.ui.adapters.TrackAdapter;
import com.example.mp3player.viewmodels.SearchViewModel;

public class SearchFragment extends Fragment {
    private SearchViewModel searchViewModel;
    private SearchView searchView;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TrackAdapter trackAdapter;
    private Handler searchHandler;
    private Runnable searchRunnable;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        searchHandler = new Handler(Looper.getMainLooper());
        
        searchView = view.findViewById(R.id.searchView);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        
        setupSearchView();
        setupRecyclerView();
        observeViewModel();
        
        return view;
    }
    
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                // Debounce search - wait 500ms after user stops typing
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                searchRunnable = () -> performSearch(newText);
                searchHandler.postDelayed(searchRunnable, 500);
                
                return true;
            }
        });
    }
    
    private void setupRecyclerView() {
        trackAdapter = new TrackAdapter(new TrackAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(Track track) {
                com.example.mp3player.MainActivity activity = (com.example.mp3player.MainActivity) requireActivity();
                // Play track with full search results as queue
                java.util.List<Track> tracks = searchViewModel.getSearchResults().getValue();
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
        
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(trackAdapter);
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
    
    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Search for music");
            rvSearchResults.setVisibility(View.GONE);
            return;
        }
        
        searchViewModel.search(query);
    }
    
    private void observeViewModel() {
        searchViewModel.getSearchResults().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null && !tracks.isEmpty()) {
                trackAdapter.updateTracks(tracks);
                rvSearchResults.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
            } else if (tracks != null) {
                rvSearchResults.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("No results found");
            }
        });
        
        searchViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        searchViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}
