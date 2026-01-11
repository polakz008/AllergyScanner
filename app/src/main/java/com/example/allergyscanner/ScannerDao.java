package com.example.allergyscanner;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ScannerDao {
    @Insert
    long insert(ScannerItem item);

    @Query("SELECT * FROM scanner WHERE userId = :userId")
    List<ScannerItem> getForUser(long userId);
}