package com.example.dylbo.musicfriend.ui;
//This activity is the song base activity and holds the song sections fragment when not playing

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.example.dylbo.musicfriend.R;
import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SectionEntry;


import java.util.ArrayList;
import java.util.List;

public class SongActivity extends AppCompatActivity {

    // Extra for the task ID to be received in the intent
    public static final String EXTRA_SONG_ID = "extraSongId";
    public static final String EXTRA_SONG_TITLE = "extraSongTitle";
    // Constant for logging
    private static final String TAG = SongActivity.class.getSimpleName();

    //General song variables

    private int mSongID; //Song id passed through when activity starts
    private String mSongTitle;//Song title passed through when activity starts
    private AppDatabase mDb;//Database

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_tabs);//Set main layout for song view
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        Log.d(TAG, "Activity");
        ////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////Get extra from intent extra/////////////////////////
        Intent intent = getIntent();
        Bundle bundle;
        bundle = intent.getExtras();//get bundle of extras
        if (intent != null) {
            mSongID = bundle.getInt(EXTRA_SONG_ID);//get song id from bundle
            Log.d(TAG, "mSongID: " + mSongID);
            mSongTitle = bundle.getString(EXTRA_SONG_TITLE);//get song title from bundle
            Log.d(TAG, "mSongTitle: " + mSongTitle);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////Set Up viewpager, tabs and fragments/////////////////////////
        ViewPager viewPager =  findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add Fragments to adapter one by one
        FragmentSongSections fragmentSongSections = new FragmentSongSections();
        fragmentSongSections.setArguments(bundle);

        // Add Fragments to adapter one by one
        FragmentSongRecordings fragmentSongRecordings = new FragmentSongRecordings();
        fragmentSongRecordings.setArguments(bundle);

        adapter.addFragment(fragmentSongSections, "SECTIONS");
        adapter.addFragment(fragmentSongRecordings, "RECORDINGS");
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.song, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    // Adapter for the viewpager using FragmentPagerAdapter
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}


