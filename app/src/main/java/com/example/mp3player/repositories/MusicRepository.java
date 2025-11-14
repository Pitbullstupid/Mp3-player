package com.example.mp3player.repositories;

import android.content.Context;

import com.example.mp3player.api.DeezerApiClient;
import com.example.mp3player.database.DatabaseHelper;
import com.example.mp3player.models.Track;

import java.util.List;

public class MusicRepository {
    private final DeezerApiClient apiClient;
    private final DatabaseHelper databaseHelper;
    
    public MusicRepository(Context context) {
        this.apiClient = new DeezerApiClient();
        this.databaseHelper = new DatabaseHelper(context);
    }
    
    /**
     * Search for tracks
     * @param query The search query
     * @param callback Callback for handling results
     */
    public void searchTracks(String query, DeezerApiClient.SearchCallback callback) {
        apiClient.searchTracks(query, callback);
    }
    
    /**
     * Get track by ID
     * @param trackId The track ID
     * @param callback Callback for handling result
     */
    public void getTrackById(long trackId, DeezerApiClient.TrackCallback callback) {
        apiClient.getTrack(trackId, callback);
    }
    
    /**
     * Get recommended tracks (chart)
     * @param callback Callback for handling results
     */
    public void getRecommendations(DeezerApiClient.ChartCallback callback) {
        apiClient.getChart(callback);
    }
    
    /**
     * Get new releases (using chart as well)
     * @param callback Callback for handling results
     */
    public void getNewReleases(DeezerApiClient.ChartCallback callback) {
        apiClient.getChart(callback);
    }
    
    /**
     * Add a track to user's library
     * @param userId The user ID
     * @param track The track to add
     * @return true if added successfully, false otherwise
     */
    public boolean addToLibrary(long userId, Track track) {
        // Check if already in library
        if (databaseHelper.isTrackInLibrary(userId, track.getId())) {
            return false;
        }
        
        long result = databaseHelper.insertLibraryTrack(userId, track);
        return result != -1;
    }
    
    /**
     * Remove a track from user's library
     * @param userId The user ID
     * @param trackId The track ID to remove
     * @return true if removed successfully, false otherwise
     */
    public boolean removeFromLibrary(long userId, long trackId) {
        int rowsDeleted = databaseHelper.deleteLibraryTrack(userId, trackId);
        return rowsDeleted > 0;
    }
    
    /**
     * Get all tracks in user's library
     * @param userId The user ID
     * @return List of tracks in the library
     */
    public List<Track> getLibraryTracks(long userId) {
        return databaseHelper.getLibraryTracks(userId);
    }
    
    /**
     * Check if a track is in user's library
     * @param userId The user ID
     * @param trackId The track ID
     * @return true if track is in library, false otherwise
     */
    public boolean isInLibrary(long userId, long trackId) {
        return databaseHelper.isTrackInLibrary(userId, trackId);
    }
}
