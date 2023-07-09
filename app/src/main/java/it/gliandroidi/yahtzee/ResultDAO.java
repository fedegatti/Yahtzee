package it.gliandroidi.yahtzee;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// DAO utilizzato per accedere al database dei risultati
@Dao
public interface ResultDAO {
    @Query("SELECT * FROM result")
    List<Result> getResultList();

    @Query("SELECT count(*) FROM result")
    int size();

    @Insert
    void insertResult(Result result);

    @Update
    void updateResult(Result result);

    @Delete
    void deleteResult(Result result);
}
