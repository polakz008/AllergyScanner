package com.example.allergyscanner;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AllergenDao {
    @Insert
    long insert(Allergen allergen);

    @Update
    void update(Allergen allergen);

    @Query("SELECT * FROM allergens WHERE userId = :userId AND selected = 1")
    List<Allergen> getSelectedAllergens(long userId);

    @Query("SELECT * FROM allergens WHERE userId = :userId")
    List<Allergen> getForUser(long userId);

    @Query("DELETE FROM allergens WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM allergens WHERE userId = :userId")
    void deleteAllForUser(long userId);
}