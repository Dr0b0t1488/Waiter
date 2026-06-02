package com.example.waiter.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.waiter.models.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE role = 'WAITER' AND establishmentCode = :code")
    LiveData<List<User>> getAllWaitersByCode(String code);

    @Query("SELECT * FROM users WHERE role = 'ADMIN' AND establishmentCode = :code")
    LiveData<List<User>> getAllAdminsByCode(String code);

    @Query("SELECT COUNT(*) > 0 FROM users WHERE role = 'ADMIN' AND establishmentCode = :code")
    boolean isAdminCodeExists(String code);

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND establishmentCode = :code")
    int getAdminCountByCode(String code);

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    @Query("SELECT * FROM users WHERE establishmentCode = :code LIMIT 1")
    User getUserByCodeNow(String code);
}
