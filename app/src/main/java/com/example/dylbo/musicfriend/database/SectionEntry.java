package com.example.dylbo.musicfriend.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(
        entity = SongEntry.class,
        parentColumns = "id",
        childColumns = "songid",
        onDelete = CASCADE),
        indices ={@Index("songid")})
public class SectionEntry {
    @PrimaryKey (autoGenerate = true)
    private int sectionID;
    @ColumnInfo
    public int songid;
    private String sectionTitle;
    private String description;
    private String bars;
    private int color;
    private int bpm;
    private Date updatedAt;

    @Ignore
    public SectionEntry(int songid, String sectionTitle, String bars,String description ,int color,int bpm, Date updatedAt) {
        this.songid = songid;
        this.bpm = bpm;
        this.description = description;
        this.bars = bars;
        this.color = color;
        this.sectionTitle = sectionTitle;
        this.updatedAt = updatedAt;
    }

    public SectionEntry(int sectionID,String sectionTitle,int songid, String bars,String description ,int color,int bpm, Date updatedAt) {
        this.sectionID = sectionID;
        this.sectionTitle = sectionTitle;
        this.songid = songid;
        this.bpm = bpm;
        this.description = description;
        this.bars = bars;
        this.color = color;
        this.updatedAt = updatedAt;
    }

    public int getSectionID() {
        return sectionID;
    }

    public int getSongID() {return songid;}

    public void setSectionId(int sectionID) {
        this.sectionID = sectionID;
    }

    public String getBars() {
        return bars;
    }

    public int getColor() {return color; }

    public int getBpm() {return bpm;}

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setColor(int color) { this.color= color ; }

    public void setBars(String bars) {
        this.bars = bars;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

}
