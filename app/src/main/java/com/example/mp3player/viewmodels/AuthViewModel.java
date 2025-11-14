package com.example.mp3player.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.mp3player.models.User;
import com.example.mp3player.repositories.UserRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> currentUser;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<Boolean> isLoading;
    private final ExecutorService executorService;
    
    public AuthViewModel(Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        this.currentUser = new MutableLiveData<>();
        this.errorMessage = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
    
    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }
    
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Login with username/email and password
     */
    public void login(String usernameOrEmail, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        executorService.execute(() -> {
            try {
                android.util.Log.d("AuthViewModel", "Attempting login for: " + usernameOrEmail);
                User user = userRepository.login(usernameOrEmail, password);
                
                if (user != null) {
                    android.util.Log.d("AuthViewModel", "Login successful");
                    currentUser.postValue(user);
                } else {
                    android.util.Log.e("AuthViewModel", "Login failed - invalid credentials");
                    errorMessage.postValue("Invalid username or password");
                }
            } catch (Exception e) {
                android.util.Log.e("AuthViewModel", "Login error", e);
                errorMessage.postValue("Login failed: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Register a new user
     */
    public void register(String username, String email, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        executorService.execute(() -> {
            try {
                android.util.Log.d("AuthViewModel", "Attempting registration for: " + username);
                User user = userRepository.register(username, email, password);
                
                if (user != null) {
                    android.util.Log.d("AuthViewModel", "Registration successful for user ID: " + user.getId());
                    currentUser.postValue(user);
                } else {
                    android.util.Log.e("AuthViewModel", "Registration returned null");
                    errorMessage.postValue("Registration failed. Please try again.");
                }
            } catch (IllegalArgumentException e) {
                android.util.Log.e("AuthViewModel", "Validation error: " + e.getMessage());
                errorMessage.postValue(e.getMessage());
            } catch (Exception e) {
                android.util.Log.e("AuthViewModel", "Registration error", e);
                errorMessage.postValue("Registration failed: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        userRepository.logout();
        currentUser.setValue(null);
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return userRepository.isLoggedIn();
    }
}
