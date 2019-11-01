package com.example.dylbo.musicfriend.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dylbo.musicfriend.R;
import com.example.dylbo.musicfriend.Utils.AppExecutors;
import com.example.dylbo.musicfriend.Utils.AudioRecordTest;
import com.example.dylbo.musicfriend.Utils.RunSong;
import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SectionEntry;
import com.example.dylbo.musicfriend.database.SongEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class PlaySongActivity extends AppCompatActivity {

    //General song variables
    private AppDatabase mDb;//Database
    private String mSongTitle;
    private int mSongID;
    private TextView mSongDisplayTitle;
    private ImageButton mSongPlayPause;
    private RunSong mRunSong;
    private boolean playing = FALSE;
    private SongEntry mSongEntry;

    private int playingMode=0;

    private AudioRecordTest mAudioRecord;

    private ArrayList selectedItems;

    private MediaPlayer mMediaPlayer;

    private FloatingActionButton mReplayAllFAB;


    public static List<SectionEntry> mSongSections;
    // Extra for the task ID to be received in the intent
    public static final String EXTRA_SONG_ID = "extraSongId";
    public static final String EXTRA_SONG_TITLE = "extraSongTitle";
    public static final String EXTRA_SECTION_TITLE = "extraSectionTitle";
    public static final String EXTRA_SECTION_DESCRIPTION= "extraSectionDescription";
    public static final String EXTRA_SECTION_COLOR = "extraSectionColor";
    public static final String EXTRA_SECTION_BARS = "extraSectionBars";


    private static final String TAG = PlaySongActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);//Set main layout for song view

        mDb = AppDatabase.getInstance(getApplicationContext());//Get instance of Database

        mAudioRecord = new AudioRecordTest(this, "","SessionRecording",mSongID);

        //Get intent and Bundle extras
        Intent intent = getIntent();
        final Bundle bundle;
        bundle = intent.getExtras();//get bundle of extras
        if (intent != null) {
            mSongID = bundle.getInt(EXTRA_SONG_ID);//get song id from bundle
            //songSectionsFragment.setSongSectionsFragmentSongId(mSongID);
            mSongTitle = bundle.getString(EXTRA_SONG_TITLE);//get song title from bundle
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        //Set SongEntry for each section and create fragments
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<SectionEntry> mSections = mDb.getSectionDao().listloadSongSections(mSongID);
                int size = mSections.size();
                //////////////////////FRAGMENT 1: playing section Code////////////////////
                //Create new instance of song sections fragment
                PlayingNextFragment playingSectionFragment = new PlayingNextFragment();
                Bundle bundle1;
                bundle1 = sectionEntryToBundle(mSections.get(0));
                playingSectionFragment.setArguments(bundle1);
                //Use fragmentManager and transaction to add fragment to the screen
                FragmentManager fragmentManager = getSupportFragmentManager();

                //Fragment transaction
                fragmentManager.beginTransaction()
                        .add(R.id.playing_section_container_FL, playingSectionFragment)
                        .commit();
                ////////////////////////////////////////////////////////////////////////////////////////////////////
                //////////////////////FRAGMENT 2: next section Code////////////////////
                //Create new instance of song sections fragment
                PlayingNextFragment nextSectionsFragment = new PlayingNextFragment();
                if(size>1) {
                    bundle1 = sectionEntryToBundle(mSections.get(1));
                    nextSectionsFragment.setArguments(bundle1);

                    //Fragment transaction
                    fragmentManager.beginTransaction()
                            .add(R.id.next_section_container_FL, nextSectionsFragment)
                            .commit();
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////////
                //       //Maybe make Recycler???
                //////////////////////FRAGMENT 3: upcoming 1 section Code////////////////////
                //Create new instance of song sections fragment
                UpcomingFragment upcomingSection1Fragment = new UpcomingFragment();
                if(size>2) {
                    bundle1 = sectionEntryToBundle(mSections.get(2));
                    upcomingSection1Fragment.setArguments(bundle1);
                    //Fragment transaction
                    fragmentManager.beginTransaction()
                            .add(R.id.upcoming_section_1_container_FL, upcomingSection1Fragment)
                            .commit();
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////////

                //////////////////////FRAGMENT 4: upcoming 2 section Code////////////////////
                //Create new instance of song sections fragment
                UpcomingFragment upcomingSection2Fragment = new UpcomingFragment();
                if(size>3) {
                    bundle1 = sectionEntryToBundle(mSections.get(3));
                    upcomingSection2Fragment.setArguments(bundle1);

                    //Fragment transaction
                    fragmentManager.beginTransaction()
                            .add(R.id.upcoming_section_2_container_FL, upcomingSection2Fragment)
                            .commit();
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////////////////////////////////
                //Start RunSong on new thread
                ////////////////////////////////////////////////////////////////////////////////////////////////////
                mSongSections = mDb.getSectionDao().listloadSongSections(mSongID);
                mRunSong = new RunSong(mSongID,mSongSections,PlaySongActivity.this,playingSectionFragment,
                        nextSectionsFragment,upcomingSection1Fragment,upcomingSection2Fragment, PlaySongActivity.this);
                //mRunSong.run();
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////////////
        //Set Song title
        mSongDisplayTitle = findViewById(R.id.song_title_play_TV);
        mSongDisplayTitle.setText(mSongTitle);
        //////////////////////FAB button setup////////////////////
         /*Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */
        mReplayAllFAB = findViewById(R.id.replay_all_FAB);//Play button FAB

        mReplayAllFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch AddTaskActivity adding the song id as an extra in the intent
                mReplayAllFAB.hide();
                mRunSong.resetSections();
                mSongPlayPause.setBackground(ContextCompat.getDrawable(PlaySongActivity.this, R.drawable.ic_pause_full_circle));
                mRunSong.restartSong();
                playing=TRUE;
                mRunSong.run();

            }
        });


        //////////////////////Play button setup////////////////////
        mSongPlayPause = findViewById(R.id.play_session_FAB);
        mSongPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch PlaySongActivity
                if (playing) {
                    switch (playingMode) {

                        case 0:
                            //Recording
                            mRunSong.playPauseSong();//Toggle Playing bool
                            mAudioRecord.stopRecording();
                            mAudioRecord.saveNewRecordingtoDB(mAudioRecord.getMfilename());
                            Toast.makeText(PlaySongActivity.this, "Recording Saved",
                                    Toast.LENGTH_LONG).show();
                            mRunSong.resetSections();
                            mSongPlayPause.setImageResource(R.drawable.ic_play);
                            playing=FALSE;
                            break;
                        case 1:
                            //Playing Audio
                            mRunSong.playPauseSong();//Toggle Playing bool
                            mReplayAllFAB.show();
                            mSongPlayPause.setImageResource(R.drawable.ic_play);
                            playing=FALSE;
                            break;
                        case 2:
                            //Metronome Only
                            mRunSong.playPauseSong();//Toggle Playing bool
                            mReplayAllFAB.show();
                            mSongPlayPause.setImageResource(R.drawable.ic_play);
                            playing=FALSE;
                            break;
                    }
                } else {
                    switch (playingMode) {
                        case 0:
                          //Recording
                            mAudioRecord.startRecording();
                            mRunSong.playPauseSong();//Toggle Playing bool
                            mRunSong.run();
                            mSongPlayPause.setImageResource(R.drawable.ic_action_stop_dark);
                            playing=TRUE;
                            mReplayAllFAB.hide();
                            break;
                        case 1:

                            //Playing Audio
                            mRunSong.playPauseSong();//Toggle Playing bool
                            mRunSong.run();
                            mSongPlayPause.setImageResource(R.drawable.ic_pause);
                            playing=TRUE;
                            mReplayAllFAB.hide();
                            break;
                        case 2:
                            //Metronome Only
                            mRunSong.playPauseSong();//Toggle Playing bool
                            mRunSong.run();
                            mSongPlayPause.setImageResource(R.drawable.ic_pause);
                            playing=TRUE;
                            mReplayAllFAB.hide();
                            break;
                    }

                }
            }
        });

    }



    public Bundle sectionEntryToBundle (SectionEntry sectionEntry){
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SECTION_TITLE, sectionEntry.getSectionTitle());
        bundle.putString(EXTRA_SECTION_DESCRIPTION, sectionEntry.getDescription());
        bundle.putString(EXTRA_SECTION_BARS, sectionEntry.getBars());
        bundle.putInt(EXTRA_SECTION_COLOR, sectionEntry.getColor());

        return bundle;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.play_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_play_record_settings:
                playRecSettingsDialgue();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void stop(){
        //mRunSong.playPauseSong();//Toggle Playing bool
        switch (playingMode) {
            case 0:
                //Recording
                mAudioRecord.stopRecording();
                mAudioRecord.saveNewRecordingtoDB(mAudioRecord.getMfilename());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlaySongActivity.this, "Recording Saved",
                                Toast.LENGTH_LONG).show();
                    }
                });
                mRunSong.resetSections();
                mSongPlayPause.setImageResource(R.drawable.ic_play);
                playing=FALSE;
                break;
            case 1:

                //Playing Audio
                mRunSong.resetSections();
                mSongPlayPause.setImageResource(R.drawable.ic_play);
                playing=FALSE;
                break;
            case 2:
                //Metronome Only
                mRunSong.resetSections();
                mSongPlayPause.setImageResource(R.drawable.ic_play);
                playing=FALSE;
                break;
        }

    }


    private void playRecSettingsDialgue(){
        selectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.MyAlertDialogStyle);
        AlertDialog dialog;
        // Set the dialog title
        builder.setTitle("Session Mode")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(R.array.testArray, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        playingMode =0;
                                        break;
                                    case 1:
                                        playingMode =1;
                                        break;
                                    case 2:
                                        playingMode =2;
                                        break;
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the selectedItems results somewhere
                        // or return them to the component that opened the dialog

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        dialog = builder.create();
        //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }
}
