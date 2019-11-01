package com.example.dylbo.musicfriend.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SongEntry;
import com.example.dylbo.musicfriend.ui.FragmentSongSections;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AudioRecordTest {

    private static final String LOG_TAG = "AudioRecordTest";

    private  String mFileLocation = null;

    private static final String TAG = AudioRecordTest.class.getSimpleName();

    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;
    private String mfilename = null;
    private Context mContext;
    private SongEntry songEntry = null;
    private int mSongID;

    private String mRecordingType;
    private AppDatabase mDb;


    public AudioRecordTest (Context context,String filename, String recordingType, int SongID){
        this.mfilename = filename;
        this.mContext = context;
        this.mRecordingType = recordingType;
        this.mSongID = SongID;
        mDb = AppDatabase.getInstance(context);

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }
    private void setupFileLocation() {

    }
    public void startRecording() {
        mFileLocation = mContext.getFilesDir().getAbsolutePath();
        Date date =  new Date();
        DateFormat df = new SimpleDateFormat("dd-MMM-yyyy-hh-mm-ss", Locale.ENGLISH);
        String recordingDate= df.format(date);
        mfilename = mFileLocation+"/"+recordingDate+ ".3gp";
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mfilename);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

    }
    public String getMfilename(){
        return mfilename;
    }

    public void saveNewRecordingtoDB(final String filename){
        //////////////////////////////////////////////////////////////////////////////////////////////
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                songEntry = mDb.getSongDao().LoadSong(mSongID);
                //Date date = new Date();
                ArrayList<String> recordings = songEntry.getRecordingFileLocations();
                Log.d(TAG, "recordings Array: "+ recordings);
                Log.d(TAG, "filename: "+ filename);
                recordings.add(0,filename);
                songEntry.setRecordingFileLocations(recordings);
                mDb.getSongDao().updateSong(songEntry);
            }
        });
    }


    /*
        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);

            // Record to the external cache directory for visibility
            mFileName = getFilesDir().getAbsolutePath();
            Date date =  new Date();
            DateFormat df = new SimpleDateFormat("dd-MMM-yyyy-HH-MM-SS", Locale.ENGLISH);
            String recordingDate= df.format(date);
            mFileName += "/"+recordingDate+ ".mp3";
            mDb = AppDatabase.getInstance(getApplicationContext());
            /////////////////////////////////////////////////////////////////////////////////////////////
            //Test code only
            //////////////////////////////////////////////////////////////////////////////////////////////
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    songEntry = mDb.getSongDao().LoadSong(0);
                    //Date date = new Date();
                    ArrayList<String> recordings = songEntry.getRecordingFileLocations();
                    Log.d(TAG, "recordings Array: "+ recordings);
                    Log.d(TAG, "filename: "+ mFileName);
                    recordings.add(mFileName);
                    songEntry.setRecordingFileLocations(recordings);
                    mDb.getSongDao().updateSong(songEntry);
                }
            });
            /////////////////////////////////////////////////////////////////////////////////////////////
            //Test code only
            //////////////////////////////////////////////////////////////////////////////////////////////

            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

            LinearLayout ll = new LinearLayout(this);
            mRecordButton = new RecordButton(this);
            ll.addView(mRecordButton,
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            0));
            mPlayButton = new PlayButton(this);
            ll.addView(mPlayButton,
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            0));
            setContentView(ll);


        }

    class RecordButton extends android.support.v7.widget.AppCompatButton  {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends android.support.v7.widget.AppCompatButton  {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }*/
}