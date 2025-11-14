package com.example.mp3player.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeezerApiClient {
    private static final String BASE_URL = "https://api.deezer.com/";
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    public DeezerApiClient() {
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }
    
    // Callback interfaces
    
    public interface SearchCallback {
        void onSuccess(List<com.example.mp3player.models.Track> tracks);
        void onError(Exception e);
    }
    
    public interface TrackCallback {
        void onSuccess(com.example.mp3player.models.Track track);
        void onError(Exception e);
    }
    
    public interface ChartCallback {
        void onSuccess(List<com.example.mp3player.models.Track> tracks);
        void onError(Exception e);
    }
    
    /**
     * Search for tracks by query
     * @param query The search query
     * @param callback Callback for handling results
     */
    public void searchTracks(String query, final SearchCallback callback) {
        String url = BASE_URL + "search?q=" + query;
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(new IOException("Unexpected code " + response));
                    return;
                }
                
                try {
                    String responseBody = response.body().string();
                    JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                    JsonArray dataArray = jsonObject.getAsJsonArray("data");
                    
                    List<com.example.mp3player.models.Track> tracks = parseTracksFromJson(dataArray);
                    callback.onSuccess(tracks);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }
    
    /**
     * Get track details by ID
     * @param trackId The track ID
     * @param callback Callback for handling result
     */
    public void getTrack(long trackId, final TrackCallback callback) {
        String url = BASE_URL + "track/" + trackId;
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(new IOException("Unexpected code " + response));
                    return;
                }
                
                try {
                    String responseBody = response.body().string();
                    JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                    
                    com.example.mp3player.models.Track track = parseTrackFromJson(jsonObject);
                    callback.onSuccess(track);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }
    
    /**
     * Get chart (popular/recommended tracks)
     * @param callback Callback for handling results
     */
    public void getChart(final ChartCallback callback) {
        String url = BASE_URL + "chart/0/tracks?limit=20";
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(new IOException("Unexpected code " + response));
                    return;
                }
                
                try {
                    String responseBody = response.body().string();
                    JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                    JsonArray dataArray = jsonObject.getAsJsonArray("data");
                    
                    List<com.example.mp3player.models.Track> tracks = parseTracksFromJson(dataArray);
                    callback.onSuccess(tracks);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }
    
    /**
     * Parse a list of tracks from JSON array
     */
    private List<com.example.mp3player.models.Track> parseTracksFromJson(JsonArray dataArray) {
        List<com.example.mp3player.models.Track> tracks = new ArrayList<>();
        
        for (int i = 0; i < dataArray.size(); i++) {
            JsonObject trackJson = dataArray.get(i).getAsJsonObject();
            com.example.mp3player.models.Track track = parseTrackFromJson(trackJson);
            tracks.add(track);
        }
        
        return tracks;
    }
    
    /**
     * Parse a single track from JSON object
     */
    private com.example.mp3player.models.Track parseTrackFromJson(JsonObject trackJson) {
        com.example.mp3player.models.Track track = new com.example.mp3player.models.Track();
        
        track.setId(trackJson.get("id").getAsLong());
        track.setTitle(trackJson.get("title").getAsString());
        track.setPreviewUrl(trackJson.get("preview").getAsString());
        track.setDuration(trackJson.get("duration").getAsInt());
        
        // Artist
        if (trackJson.has("artist")) {
            JsonObject artistJson = trackJson.getAsJsonObject("artist");
            track.setArtist(artistJson.get("name").getAsString());
        }
        
        // Album
        if (trackJson.has("album")) {
            JsonObject albumJson = trackJson.getAsJsonObject("album");
            track.setAlbum(albumJson.get("title").getAsString());
            
            if (albumJson.has("cover_medium")) {
                track.setArtworkUrl(albumJson.get("cover_medium").getAsString());
            }
        }
        
        track.setInLibrary(false);
        
        return track;
    }
}
