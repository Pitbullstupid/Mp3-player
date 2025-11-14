package com.example.mp3player.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mp3player.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_LIBRARY_TRACKS = "library_tracks";
    
    // Users table columns
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_CREATED_AT = "created_at";
    
    // Library tracks table columns
    public static final String COLUMN_LIBRARY_ID = "id";
    public static final String COLUMN_USER_ID_FK = "user_id";
    public static final String COLUMN_TRACK_ID = "track_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_ALBUM = "album";
    public static final String COLUMN_ARTWORK_URL = "artwork_url";
    public static final String COLUMN_PREVIEW_URL = "preview_url";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_ADDED_AT = "added_at";
    
    // Create users table SQL
    private static final String CREATE_USERS_TABLE = 
        "CREATE TABLE " + TABLE_USERS + " (" +
        COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
        COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, " +
        COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
        COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
        ")";
    
    // Create library_tracks table SQL
    private static final String CREATE_LIBRARY_TRACKS_TABLE = 
        "CREATE TABLE " + TABLE_LIBRARY_TRACKS + " (" +
        COLUMN_LIBRARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_USER_ID_FK + " INTEGER NOT NULL, " +
        COLUMN_TRACK_ID + " INTEGER NOT NULL, " +
        COLUMN_TITLE + " TEXT NOT NULL, " +
        COLUMN_ARTIST + " TEXT NOT NULL, " +
        COLUMN_ALBUM + " TEXT, " +
        COLUMN_ARTWORK_URL + " TEXT, " +
        COLUMN_PREVIEW_URL + " TEXT, " +
        COLUMN_DURATION + " INTEGER, " +
        COLUMN_ADDED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
        "FOREIGN KEY (" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
        "UNIQUE(" + COLUMN_USER_ID_FK + ", " + COLUMN_TRACK_ID + ")" +
        ")";
    
    // Create indexes
    private static final String CREATE_INDEX_LIBRARY_USER_ID = 
        "CREATE INDEX idx_library_user_id ON " + TABLE_LIBRARY_TRACKS + "(" + COLUMN_USER_ID_FK + ")";
    
    private static final String CREATE_INDEX_LIBRARY_TRACK_ID = 
        "CREATE INDEX idx_library_track_id ON " + TABLE_LIBRARY_TRACKS + "(" + COLUMN_TRACK_ID + ")";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_LIBRARY_TRACKS_TABLE);
        db.execSQL(CREATE_INDEX_LIBRARY_USER_ID);
        db.execSQL(CREATE_INDEX_LIBRARY_TRACK_ID);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIBRARY_TRACKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        
        // Create tables again
        onCreate(db);
    }
    
    // User database operations
    
    /**
     * Insert a new user into the database
     * @param user The user to insert
     * @return The row ID of the newly inserted user, or -1 if an error occurred
     */
    public long insertUser(com.example.mp3player.models.User user) {
        android.util.Log.d("DatabaseHelper", "insertUser called for: " + user.getUsername());
        
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            android.content.ContentValues values = new android.content.ContentValues();
            
            values.put(COLUMN_USERNAME, user.getUsername());
            values.put(COLUMN_EMAIL, user.getEmail());
            values.put(COLUMN_PASSWORD_HASH, user.getPasswordHash());
            
            android.util.Log.d("DatabaseHelper", "Attempting to insert into database");
            long id = db.insert(TABLE_USERS, null, values);
            android.util.Log.d("DatabaseHelper", "Insert returned ID: " + id);
            
            db.close();
            
            return id;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error inserting user", e);
            return -1;
        }
    }
    
    /**
     * Get a user by username
     * @param username The username to search for
     * @return The user if found, null otherwise
     */
    public com.example.mp3player.models.User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        com.example.mp3player.models.User user = null;
        
        android.database.Cursor cursor = db.query(
            TABLE_USERS,
            null,
            COLUMN_USERNAME + " = ?",
            new String[]{username},
            null, null, null
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            user = new com.example.mp3player.models.User();
            user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)));
            
            String createdAtStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
            if (createdAtStr != null) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                    user.setCreatedAt(sdf.parse(createdAtStr));
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
            }
            
            cursor.close();
        }
        
        db.close();
        return user;
    }
    
    /**
     * Get a user by email
     * @param email The email to search for
     * @return The user if found, null otherwise
     */
    public com.example.mp3player.models.User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        com.example.mp3player.models.User user = null;
        
        android.database.Cursor cursor = db.query(
            TABLE_USERS,
            null,
            COLUMN_EMAIL + " = ?",
            new String[]{email},
            null, null, null
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            user = new com.example.mp3player.models.User();
            user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)));
            
            String createdAtStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
            if (createdAtStr != null) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                    user.setCreatedAt(sdf.parse(createdAtStr));
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
            }
            
            cursor.close();
        }
        
        db.close();
        return user;
    }
    
    /**
     * Update user information
     * @param user The user with updated information
     * @return The number of rows affected
     */
    public int updateUser(com.example.mp3player.models.User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_EMAIL, user.getEmail());
        if (user.getPasswordHash() != null) {
            values.put(COLUMN_PASSWORD_HASH, user.getPasswordHash());
        }
        
        int rowsAffected = db.update(
            TABLE_USERS,
            values,
            COLUMN_USER_ID + " = ?",
            new String[]{String.valueOf(user.getId())}
        );
        
        db.close();
        return rowsAffected;
    }
    
    /**
     * Check if a username is already taken
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public boolean isUsernameTaken(String username) {
        return getUserByUsername(username) != null;
    }
    
    /**
     * Check if an email is already registered
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    public boolean isEmailTaken(String email) {
        return getUserByEmail(email) != null;
    }
    
    // Library database operations
    
    /**
     * Insert a track into user's library
     * @param userId The user ID
     * @param track The track to add
     * @return The row ID of the newly inserted track, or -1 if an error occurred
     */
    public long insertLibraryTrack(long userId, com.example.mp3player.models.Track track) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        
        values.put(COLUMN_USER_ID_FK, userId);
        values.put(COLUMN_TRACK_ID, track.getId());
        values.put(COLUMN_TITLE, track.getTitle());
        values.put(COLUMN_ARTIST, track.getArtist());
        values.put(COLUMN_ALBUM, track.getAlbum());
        values.put(COLUMN_ARTWORK_URL, track.getArtworkUrl());
        values.put(COLUMN_PREVIEW_URL, track.getPreviewUrl());
        values.put(COLUMN_DURATION, track.getDuration());
        
        long id = db.insert(TABLE_LIBRARY_TRACKS, null, values);
        db.close();
        
        return id;
    }
    
    /**
     * Delete a track from user's library
     * @param userId The user ID
     * @param trackId The track ID to remove
     * @return The number of rows deleted
     */
    public int deleteLibraryTrack(long userId, long trackId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        int rowsDeleted = db.delete(
            TABLE_LIBRARY_TRACKS,
            COLUMN_USER_ID_FK + " = ? AND " + COLUMN_TRACK_ID + " = ?",
            new String[]{String.valueOf(userId), String.valueOf(trackId)}
        );
        
        db.close();
        return rowsDeleted;
    }
    
    /**
     * Get all tracks in user's library
     * @param userId The user ID
     * @return List of tracks in the library
     */
    public java.util.List<com.example.mp3player.models.Track> getLibraryTracks(long userId) {
        java.util.List<com.example.mp3player.models.Track> tracks = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        android.database.Cursor cursor = db.query(
            TABLE_LIBRARY_TRACKS,
            null,
            COLUMN_USER_ID_FK + " = ?",
            new String[]{String.valueOf(userId)},
            null, null,
            COLUMN_ADDED_AT + " DESC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                com.example.mp3player.models.Track track = new com.example.mp3player.models.Track();
                track.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRACK_ID)));
                track.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                track.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST)));
                track.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALBUM)));
                track.setArtworkUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTWORK_URL)));
                track.setPreviewUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PREVIEW_URL)));
                track.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)));
                track.setInLibrary(true);
                
                tracks.add(track);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        db.close();
        return tracks;
    }
    
    /**
     * Check if a track is in user's library
     * @param userId The user ID
     * @param trackId The track ID to check
     * @return true if track is in library, false otherwise
     */
    public boolean isTrackInLibrary(long userId, long trackId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        android.database.Cursor cursor = db.query(
            TABLE_LIBRARY_TRACKS,
            new String[]{COLUMN_LIBRARY_ID},
            COLUMN_USER_ID_FK + " = ? AND " + COLUMN_TRACK_ID + " = ?",
            new String[]{String.valueOf(userId), String.valueOf(trackId)},
            null, null, null
        );
        
        boolean exists = cursor != null && cursor.getCount() > 0;
        
        if (cursor != null) {
            cursor.close();
        }
        
        db.close();
        return exists;
    }
}
