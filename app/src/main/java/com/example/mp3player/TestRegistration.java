package com.example.mp3player;

import android.content.Context;
import android.util.Log;

import com.example.mp3player.database.DatabaseHelper;
import com.example.mp3player.models.User;
import com.example.mp3player.utils.PasswordUtils;

public class TestRegistration {
    private static final String TAG = "TestRegistration";
    
    public static void testDatabaseInsert(Context context) {
        Log.d(TAG, "Starting database insert test");
        
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            
            User testUser = new User();
            testUser.setUsername("testuser" + System.currentTimeMillis());
            testUser.setEmail("test" + System.currentTimeMillis() + "@example.com");
            testUser.setPasswordHash(PasswordUtils.hashPassword("test123"));
            testUser.setCreatedAt(new java.util.Date());
            
            Log.d(TAG, "Created user object: " + testUser.getUsername());
            
            long userId = dbHelper.insertUser(testUser);
            
            Log.d(TAG, "Insert result: " + userId);
            
            if (userId != -1) {
                Log.d(TAG, "SUCCESS: User inserted with ID: " + userId);
            } else {
                Log.e(TAG, "FAILED: Insert returned -1");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "ERROR during test", e);
        }
    }
}
