package com.freyr.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.freyr.app.data.model.User;
import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsersLive();

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(String id);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<User> getUserByIdLive(String id);

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR LOWER(username) = LOWER(:identifier) LIMIT 1")
    User getUserByIdentifier(String identifier);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Update
    void updateUser(User user);
}
