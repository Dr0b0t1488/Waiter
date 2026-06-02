package com.example.waiter.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.waiter.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AuthViewModel extends AndroidViewModel {
    private final DatabaseReference mDatabase;
    
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> loginError = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        mDatabase = FirebaseDatabase.getInstance("https://waiter-b309e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getLoginError() {
        return loginError;
    }

    public void login(String username, String password) {
        // Hyper Admin hardcoded check
        if ("admin".equalsIgnoreCase(username) && "admin".equals(password)) {
            currentUser.postValue(new User("Гипер Админ", "admin", "HYPER_ADMIN", "ALL"));
            return;
        }

        mDatabase.child("users").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    loginError.postValue("Пользователь не найден");
                    return;
                }
                boolean found = false;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && user.getPassword().equals(password)) {
                        currentUser.postValue(user);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    loginError.postValue("Неверный пароль");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loginError.postValue("Ошибка Firebase: " + error.getMessage() + " (Код: " + error.getCode() + ")");
            }
        });
    }

    public void logout() {
        currentUser.setValue(null);
    }

    public LiveData<List<User>> getWaiters() {
        MutableLiveData<List<User>> waiters = new MutableLiveData<>();
        User user = currentUser.getValue();
        if (user != null) {
            mDatabase.child("users").orderByChild("establishmentCode").equalTo(user.getEstablishmentCode())
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<User> userList = new ArrayList<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        User u = s.getValue(User.class);
                        if (u != null && "WAITER".equals(u.getRole())) {
                            userList.add(u);
                        }
                    }
                    waiters.postValue(userList);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        return waiters;
    }

    public LiveData<List<User>> getAdmins() {
        MutableLiveData<List<User>> admins = new MutableLiveData<>();
        User user = currentUser.getValue();
        if (user != null) {
            mDatabase.child("users").orderByChild("establishmentCode").equalTo(user.getEstablishmentCode())
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<User> userList = new ArrayList<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        User u = s.getValue(User.class);
                        if (u != null && "ADMIN".equals(u.getRole())) {
                            userList.add(u);
                        }
                    }
                    admins.postValue(userList);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        return admins;
    }

    public void isCodeValid(String code, OnResultListener<String> listener) {
        mDatabase.child("users").orderByChild("establishmentCode").equalTo(code)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasAdmin = false;
                for (DataSnapshot s : snapshot.getChildren()) {
                    User u = s.getValue(User.class);
                    if (u != null && "ADMIN".equals(u.getRole())) {
                        hasAdmin = true;
                        break;
                    }
                }
                listener.onResult(hasAdmin ? "VALID" : "NOT_FOUND");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onResult("ERROR: " + error.getMessage());
            }
        });
    }

    public void getAdminCount(String code, OnResultListener<Integer> listener) {
        mDatabase.child("users").orderByChild("establishmentCode").equalTo(code)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot s : snapshot.getChildren()) {
                    User u = s.getValue(User.class);
                    if (u != null && "ADMIN".equals(u.getRole())) {
                        count++;
                    }
                }
                listener.onResult(count);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void registerUser(User user, OnResultListener<String> listener) {
        String key = mDatabase.child("users").push().getKey();
        if (key != null) {
            mDatabase.child("users").child(key).setValue(user)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onResult("SUCCESS");
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Неизвестная ошибка";
                            listener.onResult(error);
                        }
                    });
        } else {
            listener.onResult("Не удалось создать ключ в базе");
        }
    }

    public void updateWaiter(User user) {
        // Find by username (assuming unique for simplicity in this demo) or store Firebase Key in User model
        mDatabase.child("users").orderByChild("username").equalTo(user.getUsername())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    s.getRef().setValue(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void updateCurrentUser(User user) {
        updateWaiter(user);
        currentUser.postValue(user);
    }

    public void deleteWaiter(User user) {
        mDatabase.child("users").orderByChild("username").equalTo(user.getUsername())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    s.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public interface OnResultListener<T> {
        void onResult(T result);
    }
}
