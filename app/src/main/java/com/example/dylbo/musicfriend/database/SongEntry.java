package com.example.dylbo.musicfriend.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity(tableName = "song")
public class SongEntry {

    @PrimaryKey
    public int id;
    private String title;
    private ArrayList<String> recordingFileLocations;
    private Date createdAt;


    public SongEntry(int id, String title, ArrayList<String> recordingFileLocations , Date createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.recordingFileLocations = recordingFileLocations;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() { return title; }

    public ArrayList<String> getRecordingFileLocations() { return recordingFileLocations; }

    public void setRecordingFileLocations(ArrayList<String> recordingFileLocations) {
        this.recordingFileLocations=recordingFileLocations;
    }

    public Date getCreatedAt() {
        return createdAt;
    }






}