package com.example.mp3player.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp3player.R;
import com.example.mp3player.models.Track;
import com.example.mp3player.ui.adapters.TrackAdapter;
import com.example.mp3player.viewmodels.LibraryViewModel;

public class LibraryFragment extends Fragment {
    private LibraryViewModel libraryViewModel;
    private RecyclerView rvLibraryTracks;
    private TextView tvEmptyState;
    private TrackAdapter trackAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        
        libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
        
        rvLibraryTracks = view.findViewById(R.id.rvLibraryTracks);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        
        setupRecyclerView();
        observeViewModel();
        
        libraryViewModel.loadLibrary();
        
        return view;
    }
    
    private void setupRecyclerView() {
        trackAdapter = new TrackAdapter(new TrackAdapter.OnTrackClickListener() {
            @Override
            public void onTrackClick(Track track) {
                com.example.mp3player.MainActivity activity = (com.example.mp3player.MainActivity) requireActivity();
                // Play track with full library as queue
                java.util.List<Track> tracks = libraryViewModel.getLibraryTracks().getValue();
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
                Toast.makeText(requireContext(), "More options for: " + track.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Show bottom sheet with options
            }
        });
        
        rvLibraryTracks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLibraryTracks.setAdapter(trackAdapter);
        
        // Swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Track track = libraryViewModel.getLibraryTracks().getValue().get(position);
                
                libraryViewModel.removeFromLibrary(track.getId());
                Toast.makeText(requireContext(), "Removed from library", Toast.LENGTH_SHORT).show();
            }
        });
        
        itemTouchHelper.attachToRecyclerView(rvLibraryTracks);
    }
    
    private void observeViewModel() {
        libraryViewModel.getLibraryTracks().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null && !tracks.isEmpty()) {
                trackAdapter.updateTracks(tracks);
                rvLibraryTracks.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
            } else {
                rvLibraryTracks.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
        
        libraryViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        libraryViewModel.loadLibrary();
    }
}
