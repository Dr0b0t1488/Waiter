package com.example.waiter.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.waiter.database.AppDatabase;
import com.example.waiter.models.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> loginError = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        ensureAdminExists();
    }

    private void ensureAdminExists() {
        executor.execute(() -> {
            if (db.userDao().getUserCount() == 0) {
                db.userDao().insert(new User("Admin", "admin", "ADMIN"));
            }
        });
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getLoginError() {
        return loginError;
    }

    public void login(String username, String password) {
        executor.execute(() -> {
            User user = db.userDao().login(username, password);
            if (user != null) {
                currentUser.postValue(user);
            } else {
                loginError.postValue("Invalid username or password");
            }
        });
    }

    public void logout() {
        currentUser.setValue(null);
    }

    public LiveData<List<User>> getWaiters() {
        return db.userDao().getAllWaiters();
    }

    public void addWaiter(String username, String password) {
        executor.execute(() -> {
            db.userDao().insert(new User(username, password, "WAITER"));
        });
    }

    public void updateWaiter(User user) {
        executor.execute(() -> db.userDao().update(user));
    }

    public void deleteWaiter(User user) {
        executor.execute(() -> db.userDao().delete(user));
    }
}
