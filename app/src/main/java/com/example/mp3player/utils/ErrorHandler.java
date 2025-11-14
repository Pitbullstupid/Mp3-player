package com.example.mp3player.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class ErrorHandler {
    private static final String TAG = "MP3Player";
    
    /**
     * Handle API errors and display appropriate messages
     * @param context The context for displaying toast messages
     * @param e The exception that occurred
     */
    public static void handleApiError(Context context, Exception e) {
        if (e instanceof IOException) {
            showToast(context, "Network error. Please check your connection.");
        } else {
            showToast(context, "Server error. Please try again later.");
        }
        Log.e(TAG, "API Error", e);
    }
    
    /**
     * Handle database errors and display appropriate messages
     * @param context The context for displaying toast messages
     * @param e The exception that occurred
     */
    public static void handleDatabaseError(Context context, Exception e) {
        showToast(context, "Database error. Please try again.");
        Log.e(TAG, "Database Error", e);
    }
    
    /**
     * Handle playback errors and display appropriate messages
     * @param context The context for displaying toast messages
     * @param e The exception that occurred
     */
    public static void handlePlaybackError(Context context, Exception e) {
        showToast(context, "Playback error occurred. Please try another track.");
        Log.e(TAG, "Playback Error", e);
    }
    
    /**
     * Display a toast message
     * @param context The context for displaying the toast
     * @param message The message to display
     */
    private static void showToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
