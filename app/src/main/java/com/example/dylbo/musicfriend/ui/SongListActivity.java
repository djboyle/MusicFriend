package com.example.dylbo.musicfriend.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.dylbo.musicfriend.R;
import com.example.dylbo.musicfriend.Utils.AppExecutors;
import com.example.dylbo.musicfriend.Utils.MainViewModel;
import com.example.dylbo.musicfriend.adapters.SongAdapter;
import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SectionEntry;
import com.example.dylbo.musicfriend.database.SongEntry;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongListActivity extends AppCompatActivity
        implements SongAdapter.ItemClickListener, SongAdapter.ItemLongClickListener{

    private static final String TAG = SongListActivity.class.getSimpleName();


    private AppDatabase mDb;
    private List<SongEntry> mSongs;

    private RecyclerView mSongRV;
    private SongAdapter mSongAdapter;

    private String mTitle;

    private int songNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Activity");


        mDb = AppDatabase.getInstance(getApplicationContext());

        //Recyclerview

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mSongRV = findViewById(R.id.rv_song);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSongRV.setLayoutManager(layoutManager);
        mSongRV.setItemAnimator(null);

        mSongAdapter = new SongAdapter(this, this, this);
        mSongRV.setAdapter(mSongAdapter);

        FloatingActionButton fabButton = findViewById(R.id.fabSong);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SongListActivity.this,R.style.MyAlertDialogStyle);
                AlertDialog dialog;
                builder.setTitle("Title");

                // I'm using fragment here so I'm using getView() to provide ViewGroup
                // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                View viewInflated = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialogue_add_title,null, false);
                // Set up the input
                final EditText input = viewInflated.findViewById(R.id.input);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(viewInflated);

                // Set up the buttons
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mTitle = input.getText().toString();
                        //Get song column and find largest number
                        //Get All tasks and check if empty
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mSongs = mDb.getSongDao().listLoadAllSongs();
                                if (mSongs != null) {

                                    songNum = mDb.getSongDao().LoadLastID()+1;
                                    //Log.d(TAG, "songs=" + songNum);
                                } else {songNum =0;}
                                Date date = new Date();
                                ArrayList<String> recordings = new ArrayList<>();
                                Log.d(TAG, "recordings=" + recordings);
                                final SongEntry song = new SongEntry(songNum, mTitle,recordings, date);
                                Log.d(TAG, "SongID1=" + songNum);
                                mDb.getSongDao().insertSong(song);

                                //Database

                                // Create a new intent to start an AddTaskActivity
                                Intent addSongIntent = new Intent(SongListActivity.this, SongActivity.class);
                                Bundle mBundle = new Bundle();
                                mBundle.putString(SongActivity.EXTRA_SONG_TITLE, mTitle);
                                mBundle.putInt(SongActivity.EXTRA_SONG_ID, songNum);
                                addSongIntent.putExtras(mBundle);
                                startActivity(addSongIntent);
                            }
                        });
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();




                }
        });

        setupViewModel();
        createExampleSong();
    }

    private void setupViewModel() {
        //Setup initial View model
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);


        viewModel.getSongs().observe(this, new Observer<List<SongEntry>>() {
            @Override
            public void onChanged(@Nullable List<SongEntry> songEntries) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mSongAdapter.setSongs(songEntries);
                mSongs=songEntries;
            }

        });

    }
    @Override
    public void onItemClickListener(final int position) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                SongEntry song;
                Intent addSongIntent = new Intent(SongListActivity.this, SongActivity.class);
                song = mDb.getSongDao().LoadSong(position);
                Bundle mBundle = new Bundle();
                mBundle.putString(SongActivity.EXTRA_SONG_TITLE, song.getTitle());
                mBundle.putInt(SongActivity.EXTRA_SONG_ID, song.getId());
                addSongIntent.putExtras(mBundle);
                startActivity(addSongIntent);
            }
            });

    }
    @Override
    public void onItemLongClicked(final int position) {
        // Delete song
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<SongEntry> mSongs = mSongAdapter.getSongs();
                mDb.getSongDao().deleteSong(mSongs.get(position));
            }
        });
    }
     private void createExampleSong() {
        //Data for initital song
         final String[] SectionTitles = new String[]{"Intro","Verse 1","Chorus","Verse 2","Outro"};
         final String[] SectionBars = new String[]{"2","6","8","6","2"};
         final String[] SectionDescription = new String[]{"Drums\nGuitar\nBass\nKeys","Drums\nGuitar\nBass\nKeys",
                 "Drums\nGuitar\nBass\nKeys","Drums\nGuitar\nBass\nKeys","Drums\nGuitar\nBass\nKeys"};
         final int[] SectionColor = new int[]{-11962625,-11927553,-10420407,-11959,-46775};
         final int BPM= 110;
         // Check if there are any songs in DB
         AppExecutors.getInstance().diskIO().execute(new Runnable() {
             @Override
             public void run() {
                 List<SongEntry> mSongs = mDb.getSongDao().listLoadAllSongs();
                 //Log.d(TAG, "mSongs" +mSongs.size());
                 if (mSongs.size()==0){
                     //Create sample Song
                     final Date date = new Date();
                     ArrayList<String> recordings = new ArrayList<String>();
                     //recordings.add("test");
                     Log.d(TAG, "recordings=" + recordings);
                     final SongEntry song = new SongEntry(0, "Example Song",recordings, date);
                     mDb.getSongDao().insertSong(song);
                     //Create Sample Sections
                     for (int i = 0; i < 5; i++) {
                         final SectionEntry section = new SectionEntry(0,SectionTitles[i], SectionBars[i],SectionDescription[i],SectionColor[i],BPM, date);
                         mDb.getSectionDao().insertSection(section);
                     }
                 }


             }
         });
     }
}
