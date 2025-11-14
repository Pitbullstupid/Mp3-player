package com.example.mp3player.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.mp3player.api.DeezerApiClient;
import com.example.mp3player.models.Track;
import com.example.mp3player.repositories.MusicRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final MusicRepository musicRepository;
    private final MutableLiveData<List<Track>> recommendedTracks;
    private final MutableLiveData<List<Track>> newReleases;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    
    public HomeViewModel(Application application) {
        super(application);
        this.musicRepository = new MusicRepository(application);
        this.recommendedTracks = new MutableLiveData<>();
        this.newReleases = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
    }
    
    public MutableLiveData<List<Track>> getRecommendedTracks() {
        return recommendedTracks;
    }
    
    public MutableLiveData<List<Track>> getNewReleases() {
        return newReleases;
    }
    
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public void loadRecommendations() {
        isLoading.setValue(true);
        
        musicRepository.getRecommendations(new DeezerApiClient.ChartCallback() {
            @Override
            public void onSuccess(List<Track> tracks) {
                recommendedTracks.postValue(tracks);
                isLoading.postValue(false);
            }
            
            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Failed to load recommendations: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
    
    public void loadNewReleases() {
        musicRepository.getNewReleases(new DeezerApiClient.ChartCallback() {
            @Override
            public void onSuccess(List<Track> tracks) {
                newReleases.postValue(tracks);
            }
            
            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Failed to load new releases: " + e.getMessage());
            }
        });
    }
    
    public void refreshData() {
        loadRecommendations();
        loadNewReleases();
    }
}
