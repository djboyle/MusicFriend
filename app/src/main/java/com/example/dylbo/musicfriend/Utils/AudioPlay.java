package com.example.dylbo.musicfriend.Utils;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;

import com.example.dylbo.musicfriend.ui.FragmentSongRecordings;
import com.example.dylbo.musicfriend.ui.FragmentSongSections;

import java.io.IOException;

public class AudioPlay {
    private String mAudioFileLocation;
    public MediaPlayer mediaPlayer;
    private SeekBar mSeekBar;
    private Handler mHandler = new Handler();
    //Make sure you update Seekbar on UI thread

    private static final String TAG = AudioPlay.class.getSimpleName();

    public AudioPlay (String audioFileLocation){
        this.mAudioFileLocation = audioFileLocation;
    }


    public void start()
    {//audio, then run.
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mAudioFileLocation);
            Log.d(TAG, "fileloctaion:" + mAudioFileLocation);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }



    }

    public void stop() {
        try{
            mediaPlayer.stop();
        }catch (Exception e) {
            Log.e(TAG, "mediaPlayer.stop( failed");
        }

        mediaPlayer.release();

    }

    public void pause() {//The cancel call won't kill the loop. It waits for it to end. stopAudio() shuts down audio but the loop is kept in background.
        mediaPlayer.pause();
    }

    public void resume() {//resume audio track and clickertask from pause
        mediaPlayer.start();
    }
}
