package com.example.mp3player.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mp3player.database.DatabaseHelper;
import com.example.mp3player.models.User;
import com.example.mp3player.utils.PasswordUtils;
import com.example.mp3player.utils.ValidationUtils;

public class UserRepository {
    private static final String PREFS_NAME = "MP3PlayerPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    
    private final DatabaseHelper databaseHelper;
    private final SharedPreferences sharedPreferences;
    private User currentUser;
    
    public UserRepository(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadCurrentUser();
    }
    
    /**
     * Register a new user
     * @param username The username
     * @param email The email
     * @param password The plain text password
     * @return The registered user, or null if registration failed
     * @throws IllegalArgumentException if validation fails
     */
    public User register(String username, String email, String password) {
        android.util.Log.d("UserRepository", "Starting registration for: " + username);
        
        // Validate inputs
        if (!ValidationUtils.isValidUsername(username)) {
            android.util.Log.e("UserRepository", "Invalid username: " + username);
            throw new IllegalArgumentException("Invalid username. Must be 3-20 alphanumeric characters.");
        }
        
        if (!ValidationUtils.isValidEmail(email)) {
            android.util.Log.e("UserRepository", "Invalid email: " + email);
            throw new IllegalArgumentException("Invalid email format.");
        }
        
        if (!ValidationUtils.isValidPassword(password)) {
            android.util.Log.e("UserRepository", "Invalid password length");
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        
        // Check if username or email already exists
        if (databaseHelper.isUsernameTaken(username)) {
            android.util.Log.e("UserRepository", "Username already taken: " + username);
            throw new IllegalArgumentException("Username already exists.");
        }
        
        if (databaseHelper.isEmailTaken(email)) {
            android.util.Log.e("UserRepository", "Email already taken: " + email);
            throw new IllegalArgumentException("Email already registered.");
        }
        
        android.util.Log.d("UserRepository", "Validation passed, hashing password");
        
        // Hash password
        String passwordHash = PasswordUtils.hashPassword(password);
        
        android.util.Log.d("UserRepository", "Password hashed, creating user object");
        
        // Create user object
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setCreatedAt(new java.util.Date());
        
        // Insert into database
        android.util.Log.d("UserRepository", "Inserting user into database");
        long userId = databaseHelper.insertUser(user);
        
        android.util.Log.d("UserRepository", "Insert result: " + userId);
        
        if (userId != -1) {
            user.setId(userId);
            saveCurrentUser(user);
            android.util.Log.d("UserRepository", "Registration successful, user ID: " + userId);
            return user;
        }
        
        android.util.Log.e("UserRepository", "Database insert failed, returned -1");
        return null;
    }
    
    /**
     * Login with username/email and password
     * @param usernameOrEmail The username or email
     * @param password The plain text password
     * @return The logged in user, or null if login failed
     */
    public User login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() || password == null) {
            return null;
        }
        
        User user = null;
        
        // Try to get user by username first
        user = databaseHelper.getUserByUsername(usernameOrEmail);
        
        // If not found, try by email
        if (user == null) {
            user = databaseHelper.getUserByEmail(usernameOrEmail);
        }
        
        // Verify password
        if (user != null && PasswordUtils.checkPassword(password, user.getPasswordHash())) {
            saveCurrentUser(user);
            return user;
        }
        
        return null;
    }
    
    /**
     * Logout the current user
     */
    public void logout() {
        currentUser = null;
        sharedPreferences.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .apply();
    }
    
    /**
     * Get the current logged in user
     * @return The current user, or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if a user is currently logged in
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Update user information
     * @param user The user with updated information
     * @return true if update was successful, false otherwise
     */
    public boolean updateUser(User user) {
        int rowsAffected = databaseHelper.updateUser(user);
        
        if (rowsAffected > 0 && currentUser != null && currentUser.getId() == user.getId()) {
            currentUser = user;
            saveCurrentUser(user);
            return true;
        }
        
        return false;
    }
    
    /**
     * Save current user to SharedPreferences
     */
    private void saveCurrentUser(User user) {
        currentUser = user;
        sharedPreferences.edit()
                .putLong(KEY_USER_ID, user.getId())
                .putString(KEY_USERNAME, user.getUsername())
                .apply();
    }
    
    /**
     * Load current user from SharedPreferences
     */
    private void loadCurrentUser() {
        long userId = sharedPreferences.getLong(KEY_USER_ID, -1);
        String username = sharedPreferences.getString(KEY_USERNAME, null);
        
        if (userId != -1 && username != null) {
            currentUser = databaseHelper.getUserByUsername(username);
        }
    }
}
