package com.example.waiter.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.waiter.models.Table;

import java.util.List;

@Dao
public interface TableDao {
    @Query("SELECT * FROM tables")
    LiveData<List<Table>> getAllTables();

    @Query("SELECT * FROM tables")
    List<Table> getAllTablesNow();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTable(Table table);

    @Update
    void updateTable(Table table);

    @Query("SELECT * FROM tables WHERE id = :tableId")
    Table getTableById(int tableId);
}
