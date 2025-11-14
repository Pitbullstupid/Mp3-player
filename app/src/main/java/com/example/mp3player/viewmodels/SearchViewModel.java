package com.example.mp3player.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.mp3player.api.DeezerApiClient;
import com.example.mp3player.models.Track;
import com.example.mp3player.repositories.MusicRepository;

import java.util.List;

public class SearchViewModel extends AndroidViewModel {
    private final MusicRepository musicRepository;
    private final MutableLiveData<List<Track>> searchResults;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    
    public SearchViewModel(Application application) {
        super(application);
        this.musicRepository = new MusicRepository(application);
        this.searchResults = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
    }
    
    public MutableLiveData<List<Track>> getSearchResults() {
        return searchResults;
    }
    
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            searchResults.setValue(null);
            return;
        }
        
        isLoading.setValue(true);
        
        musicRepository.searchTracks(query, new DeezerApiClient.SearchCallback() {
            @Override
            public void onSuccess(List<Track> tracks) {
                searchResults.postValue(tracks);
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Search failed: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    public void clearResults() {
        searchResults.setValue(null);
    }
}
