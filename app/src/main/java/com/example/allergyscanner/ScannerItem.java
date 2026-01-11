package com.example.allergyscanner;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
@Entity(tableName = "scanner",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE))
public class ScannerItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String code;
    public long userId;

    public ScannerItem(String code, long userId) {
        this.code = code;
        this.userId = userId;
    }
}