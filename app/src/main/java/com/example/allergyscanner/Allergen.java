package com.example.allergyscanner;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "allergens",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE))
public class Allergen {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public long userId;
    public boolean selected;

    public Allergen(String name, long userId, boolean selected) {
        this.name = name;
        this.userId = userId;
        this.selected = selected;
    }
}