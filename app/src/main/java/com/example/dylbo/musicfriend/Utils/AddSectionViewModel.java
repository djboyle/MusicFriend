package com.example.dylbo.musicfriend.Utils;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.widget.SectionIndexer;

import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SectionEntry;


// COMPLETED (5) Make this class extend ViewModel
public class AddSectionViewModel extends ViewModel {

    // COMPLETED (6) Add a task member variable for the TaskEntry object wrapped in a LiveData
    private LiveData<SectionEntry> section;

    // COMPLETED (8) Create a constructor where you call loadTaskById of the taskDao to initialize the tasks variable
    // Note: The constructor should receive the database and the taskId
    public AddSectionViewModel(AppDatabase database, int sectionId) {
        section = database.getSectionDao().loadSectionById(sectionId);
    }

    // COMPLETED (7) Create a getter for the task variable
    public LiveData<SectionEntry> getSection() {
        return section;
    }
}
