package com.example.dylbo.musicfriend.Utils;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.dylbo.musicfriend.database.AppDatabase;


// COMPLETED (1) Make this class extend ViewModel ViewModelProvider.NewInstanceFactory
public class AddSectionViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    // COMPLETED (2) Add two member variables. One for the database and one for the taskId
    private final AppDatabase mDb;
    private final int mSectionId;

    // COMPLETED (3) Initialize the member variables in the constructor with the parameters received
    public AddSectionViewModelFactory(AppDatabase database, int sectionId) {
        mDb = database;
        mSectionId = sectionId;
    }

    // COMPLETED (4) Uncomment the following method
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddSectionViewModel(mDb, mSectionId);
    }
}
