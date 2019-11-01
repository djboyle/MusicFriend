package com.example.dylbo.musicfriend.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.widget.LinearLayout;

import java.util.List;

@Dao
public interface SongDao {


    @Insert
    void insertSong(SongEntry songEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateSong(SongEntry songEntry);

    @Delete
    void deleteSong(SongEntry songEntry);

    @Query("SELECT * FROM song ORDER BY title")
    LiveData<List<SongEntry>> loadAllSongs();

    @Query("SELECT * FROM song ORDER BY title")
    List<SongEntry> listLoadAllSongs();

    @Query("SELECT * FROM song WHERE id = :id")
    SongEntry LoadSong(int id);

    @Query("SELECT id, MAX(id) FROM song")
    int LoadLastID();


}
