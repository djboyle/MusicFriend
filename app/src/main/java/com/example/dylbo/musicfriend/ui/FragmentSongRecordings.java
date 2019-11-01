package com.example.dylbo.musicfriend.ui;

import android.Manifest;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dylbo.musicfriend.R;
import com.example.dylbo.musicfriend.Utils.AppExecutors;
import com.example.dylbo.musicfriend.Utils.AudioPlay;
import com.example.dylbo.musicfriend.Utils.AudioRecordTest;
import com.example.dylbo.musicfriend.Utils.MainViewModel;
import com.example.dylbo.musicfriend.adapters.RecordingsAdapter;
import com.example.dylbo.musicfriend.adapters.SectionAdapter;
import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SectionEntry;
import com.example.dylbo.musicfriend.database.SongEntry;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class FragmentSongRecordings extends Fragment
        implements RecordingsAdapter.ItemClickListener, RecordingsAdapter.ItemLongClickListener{
    // Constant for logging
    private static final String TAG = FragmentSongSections.class.getSimpleName();

    //Define views and adapter
    private RecyclerView mRecordingsRV;
    private RecordingsAdapter mRecordingsAdapter;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private ImageButton mRecordButton;
    private ImageView mRecordingsScreen;
    private SeekBar mSeekBar;
    private TextView mQuickRecordingTV;
    private TextView mRECTV;



    private int mRecordingDuration;
    private int mProgress=0;
    private Timer timer; //Timer used in play function

    private boolean playing= FALSE;
    private boolean firstPlay=TRUE;
    private boolean mREC_EN_FLAG = FALSE;
    private boolean recording = FALSE;
    private int mActiveSongIndex = 0;
    private int mRecordingListSize=0;

    private AppDatabase mDb;//Database

    private AudioPlay mAudioPlay;
    private AudioRecordTest mAudioRecordTest;
    private ArrayList<String> mRecordingLocations;
    private Chronometer mChronometer;

    // Extra for the task ID to be received in the intent
    public static final String EXTRA_SONG_ID = "extraSongId";

    private int mSongID;

    private String mRecordingFilename;

    public FragmentSongRecordings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDb = AppDatabase.getInstance(getActivity());//Get instance of Database

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_song_recordings, container, false);
        mSongID = getArguments().getInt(EXTRA_SONG_ID);

        mAudioRecordTest = new AudioRecordTest(getActivity(), "","QuickRecording",mSongID);
        mRecordingsRV = rootView.findViewById(R.id.rv_recordings_list);
        mSeekBar = rootView.findViewById(R.id.recording_play_SB);
        mRecordingsScreen = rootView.findViewById(R.id.recording_screen_IV);
        mChronometer = rootView.findViewById(R.id.recording_chronometer);
        mRECTV = rootView.findViewById(R.id.REC_TV);
        mQuickRecordingTV = rootView.findViewById(R.id.quickRecordingTV);

        //Set up linear manager for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecordingsRV.setLayoutManager(layoutManager);
        mRecordingsRV.setItemAnimator(null);

        //Attache adapter
        mRecordingsAdapter = new RecordingsAdapter(getActivity(), this, this);
        mRecordingsRV.setAdapter(mRecordingsAdapter);

        //////////////////////Play button setup////////////////////

        mPlayButton = rootView.findViewById(R.id.play_pause_rec_IB);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch PlaySongActivity
                if (mRecordingLocations !=null &&mRecordingLocations.size()!=0){
                    if(firstPlay){
                        initialRecordingPlay();
                    }else {
                        if (playing) {
                            mPlayButton.setImageResource(R.drawable.ic_play_full_circle);
                            mAudioPlay.pause();
                            playing=FALSE;

                        } else {
                            mPlayButton.setImageResource(R.drawable.ic_pause_full_circle);
                            mAudioPlay.resume();
                            playing=TRUE;
                        }
                    }

                }
            }
        });

        //////////////////////Next button setup////////////////////
        mNextButton = rootView.findViewById(R.id.next_IB);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecordingListSize = mRecordingLocations.size();
                Log.d(TAG, "mRecordingListSize:" + mRecordingListSize);
                if(mRecordingListSize>1){
                    Log.d(TAG, "mActiveSongIndex:" + mActiveSongIndex);
                    if(mActiveSongIndex<mRecordingListSize-1){
                        mActiveSongIndex++;
                        mRecordingsAdapter.setmActiveRecording(mActiveSongIndex);
                        if(mAudioPlay != null) {
                            if (mAudioPlay.mediaPlayer != null) {
                                mAudioPlay.stop();
                            }
                        }
                        initialRecordingPlay();
                    }
                }
            }
        });

        //////////////////////Previous button setup////////////////////
        mPreviousButton = rootView.findViewById(R.id.previous_IB);
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!firstPlay){
                    Log.d(TAG, "LOG! : !firstPlay" );
                    if(mAudioPlay != null) {
                        Log.d(TAG, "mAudioPlay != null" );
                        if (mAudioPlay.mediaPlayer != null) {
                            if(mAudioPlay.mediaPlayer.isPlaying()){
                                Log.d(TAG, "duration:" + mAudioPlay.mediaPlayer.getCurrentPosition());

                                if(mAudioPlay.mediaPlayer.getCurrentPosition()<2001&&mActiveSongIndex!=0){
                                    mActiveSongIndex--;
                                    mRecordingsAdapter.setmActiveRecording(mActiveSongIndex);
                                }
                            }
                            mAudioPlay.stop();
                        }
                    }
                    initialRecordingPlay();
                }
            }
        });

        //////////////////////Seek BAr setup////////////////////
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mAudioPlay != null) {
                    if (mAudioPlay.mediaPlayer != null&& fromUser) {
                        mAudioPlay.mediaPlayer.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //////////////////////REC ENABLE FAB button setup////////////////////


        final FloatingActionButton fabButton = rootView.findViewById(R.id.REC_enable_FAB);//Play button FAB

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch AddTaskActivity adding the song id as an extra in the intent
                if(mREC_EN_FLAG) {
                    fabButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(),R.color.colorRECred)));
                    mREC_EN_FLAG=FALSE;
                    if(recording) {
                        recording = FALSE;
                        mRecordButton.setImageResource(R.drawable.ic_action_rec);
                        mAudioRecordTest.stopRecording();
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.stop();
                    }


                }else{
                    if(mAudioPlay != null) {
                        if (mAudioPlay.mediaPlayer != null) {
                            mAudioPlay.stop();
                            mPlayButton.setImageResource(R.drawable.ic_play_full_circle);
                            firstPlay=TRUE;
                            playing=FALSE;
                            mSeekBar.setProgress(0);
                            timer.cancel();
                        }
                    }
                    playing =FALSE;
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    fabButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(),R.color.colorBlack)));
                    mREC_EN_FLAG=TRUE;
                }
                recordPlayToggleVisibility();
            }
        });


        //////////////////////REC Button setup////////////////////
        mRecordButton = rootView.findViewById(R.id.rec_stop_IB);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //IF already recording
                if(recording) {
                    recording = FALSE;
                    mRecordButton.setImageResource(R.drawable.ic_action_rec);
                    mChronometer.stop();//stop chronometer
                    mAudioRecordTest.stopRecording();//stop recording
                    //Open dialogue to name file
                    renameSongDialogue();

                }else{
                    recording = TRUE;
                    mRecordButton.setImageResource(R.drawable.ic_action_stop);
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    mAudioRecordTest.startRecording();
                }
            }
        });
        setupViewModel();
        return rootView;

    }

    private void setupViewModel() {
        //Setup initial View model
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        //Observe for any changes to song sections
        viewModel.getSongs().observe(this, new Observer<List<SongEntry>>() {
            @Override
            public void onChanged(@Nullable List<SongEntry> songEntries) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mRecordingLocations = songEntries.get(mSongID).getRecordingFileLocations();
                mRecordingsAdapter.setRecordingsLocation(mRecordingLocations, mSongID);
                //mSongs=songEntries;
            }
        });

    }

    private void initialRecordingPlay(){
        if(timer!=null){
            timer.cancel();
        }
        mAudioPlay = new AudioPlay(mRecordingLocations.get(mActiveSongIndex));
        mPlayButton.setImageResource(R.drawable.ic_pause_full_circle);
        mAudioPlay.start();
        firstPlay =FALSE;
        playing=TRUE;
        mRecordingDuration= mAudioPlay.mediaPlayer.getDuration();

        mSeekBar.setMax(mRecordingDuration);

        ////Set on complete listener
        mAudioPlay.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                mAudioPlay.stop();
                mPlayButton.setImageResource(R.drawable.ic_play_full_circle);
                firstPlay=TRUE;
                playing=FALSE;
                mSeekBar.setProgress(mRecordingDuration);
                timer.cancel();
            }
        });

        timer = new Timer();        // A thread of execution is instantiated
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mAudioPlay.mediaPlayer != null && playing) {
                    try {
                        int mCurrentPosition = mAudioPlay.mediaPlayer.getCurrentPosition();
                        Log.d(TAG, "currentPosition:" + mCurrentPosition);
                        mSeekBar.setProgress(mCurrentPosition);
                    }catch (Exception e) {
                        Log.e(TAG, "setProgress() failed");
                    }
                }
            }
        }, 0, 1000);
    }

    private void recordPlayToggleVisibility(){
        if(mREC_EN_FLAG){
            mPreviousButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.GONE);
            mSeekBar.setVisibility(View.INVISIBLE);
            mRecordingsScreen.setVisibility(View.VISIBLE);
            mRecordButton.setVisibility(View.VISIBLE);
            mChronometer.setVisibility(View.VISIBLE);
            mQuickRecordingTV.setVisibility(View.VISIBLE);
            mRECTV.setVisibility(View.VISIBLE);
        }else{
            mPreviousButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);
            mSeekBar.setVisibility(View.VISIBLE);
            mRecordingsScreen.setVisibility(View.GONE);
            mRecordButton.setVisibility(View.GONE);
            mChronometer.setVisibility(View.GONE);
            mQuickRecordingTV.setVisibility(View.GONE);
            mRECTV.setVisibility(View.GONE);

        }
    }

    private void renameRecordingFile( String newFileName){
        File recording = new File(mAudioRecordTest.getMfilename());
        String directory = recording.getParent();
        File newName = new File(directory,"/" +newFileName + ".3gp");
        if(recording.renameTo(newName)){
            System.out.println("Succes! Name changed to: " + recording.getName());
        }else{
            System.out.println("failed");
        }
        mAudioRecordTest.saveNewRecordingtoDB(newName.getPath());
    }
    @Override
    public void onItemClickListener(int position) {
        // Stop any currently playing recording and start selected recording
        if(!mREC_EN_FLAG) {
            Log.d(TAG, "did this happen");
            if (mAudioPlay != null) {
                if (mAudioPlay.mediaPlayer != null) {
                    mAudioPlay.stop();
                }
            }
            mActiveSongIndex = position;
            mRecordingsAdapter.setmActiveRecording(mActiveSongIndex);
            initialRecordingPlay();
        }
    }

    @Override
    public void onItemLongClicked(final int position) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent


    }

    private void renameSongDialogue() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.MyAlertDialogStyle);
        AlertDialog dialog;
        builder.setTitle("Recording Name");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialogue_add_title,null, false);
        // Set up the input
        final EditText input = viewInflated.findViewById(R.id.input);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mRecordingFilename = input.getText().toString();
                String mFileLocation = getContext().getFilesDir().getAbsolutePath();
                String filePath =mFileLocation+"/"+mRecordingFilename+".3gp";
                //Log.d(TAG, "mRecordingFilename" + filePath);
                //Log.d(TAG, "mRecordingLocations" + mRecordingLocations);

                if (mRecordingLocations.contains(filePath)){
                    Toast.makeText(getActivity(), "Filename Already Exists",
                            Toast.LENGTH_LONG).show();
                    Date date =  new Date();
                    DateFormat df = new SimpleDateFormat("dd-MMM-yyyy-hh-mm-ss", Locale.ENGLISH);
                    String recordingDate= df.format(date);
                    renameRecordingFile(recordingDate);

                }else {
                    dialog.dismiss();
                    int check =mRecordingFilename.length();

                    if(check!=0){
                        renameRecordingFile(mRecordingFilename);
                        Log.d(TAG, "mRecordingFilename" + mRecordingFilename);
                    }else{
                        Date date =  new Date();
                        DateFormat df = new SimpleDateFormat("dd-MMM-yyyy-hh-mm-ss", Locale.ENGLISH);
                        String recordingDate= df.format(date);
                        renameRecordingFile(recordingDate);
                    }
                }
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
}

