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
public interface SectionDao {


    @Insert
    void insertSection(SectionEntry sectionEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateSection(SectionEntry sectionEntry);

    @Delete
    void deleteSection(SectionEntry sectionEntry);

    @Query("SELECT * FROM SectionEntry WHERE sectionID = :sectionID")
    LiveData<SectionEntry> loadSectionById(int sectionID);

    @Query("SELECT * FROM SectionEntry WHERE songid = :songid ORDER BY sectionID")
    LiveData<List<SectionEntry>> loadSongSections(int songid);

    @Query("SELECT * FROM SectionEntry WHERE songid = :songid ORDER BY sectionID")
    List<SectionEntry> listloadSongSections(int songid);

}
