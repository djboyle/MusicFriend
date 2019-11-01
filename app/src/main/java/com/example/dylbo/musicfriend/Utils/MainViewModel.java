package com.example.dylbo.musicfriend.Utils;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SectionEntry;
import com.example.dylbo.musicfriend.database.SongEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<SectionEntry>> sections;
    private LiveData<List<SongEntry>> songs;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        songs = database.getSongDao().loadAllSongs();
    }

    public LiveData<List<SectionEntry>> getSongSections(int songid) {
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        sections = database.getSectionDao().loadSongSections(songid);
        return sections;
    }
    public LiveData<List<SongEntry>> getSongs() {
        return songs;
    }
}
