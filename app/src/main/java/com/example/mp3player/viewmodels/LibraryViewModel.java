package com.example.mp3player.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.mp3player.models.Track;
import com.example.mp3player.repositories.MusicRepository;
import com.example.mp3player.repositories.UserRepository;

import java.util.List;

public class LibraryViewModel extends AndroidViewModel {
    private final MusicRepository musicRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<List<Track>> libraryTracks;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    
    public LibraryViewModel(Application application) {
        super(application);
        this.musicRepository = new MusicRepository(application);
        this.userRepository = new UserRepository(application);
        this.libraryTracks = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
    }
    
    public MutableLiveData<List<Track>> getLibraryTracks() {
        return libraryTracks;
    }
    
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public void loadLibrary() {
        isLoading.setValue(true);
        if (userRepository.getCurrentUser() == null) {
            errorMessage.setValue("Please login to view your library");
            return;
        }
        
        long userId = userRepository.getCurrentUser().getId();
        List<Track> tracks = musicRepository.getLibraryTracks(userId);
        libraryTracks.setValue(tracks);
        isLoading.setValue(false);
    }
    
    public void addToLibrary(Track track) {
        if (userRepository.getCurrentUser() == null) {
            errorMessage.setValue("Please login to add to library");
            return;
        }
        
        long userId = userRepository.getCurrentUser().getId();
        int result = musicRepository.addToLibrary(userId, track);
        
        if (result == 1) {
            loadLibrary();
            errorMessage.setValue("Added to library");
        } else if (result == 0) {
            errorMessage.setValue("Track already in library");
        } else {
            errorMessage.setValue("Failed to add to library");
        }
    }
    
    public void removeFromLibrary(long trackId) {
        if (userRepository.getCurrentUser() == null) {
            errorMessage.setValue("Please login");
            return;
        }
        
        long userId = userRepository.getCurrentUser().getId();
        boolean success = musicRepository.removeFromLibrary(userId, trackId);
        
        if (success) {
            loadLibrary();
        } else {
            errorMessage.setValue("Failed to remove track");
        }
    }
    
    public boolean isInLibrary(long trackId) {
        if (userRepository.getCurrentUser() == null) {
            return false;
        }
        
        long userId = userRepository.getCurrentUser().getId();
        return musicRepository.isInLibrary(userId, trackId);
    }
}
