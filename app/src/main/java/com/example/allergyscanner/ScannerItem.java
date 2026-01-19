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

    public String name;    // Zmieniono z 'code', aby przechowywać nazwę produktu
    public long date;      // Dodano pole daty
    public boolean isSafe; // Dodano pole bezpieczeństwa
    public long userId;

    // Konstruktor używany w ScanningActivity
    public ScannerItem(String name, long date) {
        this.name = name;
        this.date = date;
    }
}