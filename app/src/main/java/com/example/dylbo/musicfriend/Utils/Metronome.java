package com.example.dylbo.musicfriend.Utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;//We need to know the build version, how to launch several asynctask depends on it

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

//import android.util.Log;

/** The worker class */
public class Metronome
{
    protected double bpm;
    protected double bar;
    int beat = 0;
    protected Context context;
    protected float ticFreq, tocFreq;
    protected int ticChoice, tocChoice, ticLength, tocLength; // ticLength is how long the sound last before silence
    static protected boolean isRunning; /**< to control the while loop from outside the Task because canceling the task won't kill the loop. Declared as static so we can control every clicker at once (polyrhythm start/stop. I am not sure what would happen if we instantiated this variable, so I always access it as a class member (see setIsRunning(). We also need this class member to control every clicker at once (with polyrhythms)*/

protected int sampleRate = 44100;//This now hardcoded because having this in the settings as an EditText wasn't a good idea. It may be back someday as a spinner with 3-4 choices. Keep the variable along the way is convenient for this.
    protected AudioTrack audioTrack;
    protected ClickerTask clickerTask;
    protected byte [] tic;
    protected byte [] toc;

    public Metronome(double bpm, double bar, Context context)
    {
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        this.ticChoice = settings.getInt("tic",0);
        this.tocChoice = settings.getInt("toc",0);
        //this.ticOffset = settings.getInt("ticOffset",0);
        this.ticFreq = settings.getFloat("ticFreq",880);
        this.ticLength = settings.getInt("ticLength",50);
        //this.tocOffset = settings.getInt("tocOffset",0);
        this.tocFreq = settings.getFloat("tocFreq",660);
        this.tocLength = settings.getInt("tocLength",50);

        this.bpm = bpm;
        this.bar = bar;
        this.context = context;

        initSignal();
        clickerTask = new ClickerTask();
    }

    protected void initSignal()
    {
        tic = Signal.getSignal(context,ticChoice,ticFreq,ticLength,sampleRate,bpm);
        toc = Signal.getSignal(context,tocChoice,tocFreq,tocLength,sampleRate,bpm);
    }

    /* The task handling Clicker's main loop. */
    protected class ClickerTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {run(); return null;}
    }

    public void start()
    {//audio, then run.

        startAudio();
        Metronome.setIsRunning(true);
        if(Build.VERSION.SDK_INT >= 11)
            clickerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            clickerTask.execute();
    }

    public void stop()
    {//The cancel call won't kill the loop. It waits for it to end. stopAudio() shuts down audio but the loop is kept in background.
        //We need to end the loop, then close the thread.
        Metronome.setIsRunning(false);
        clickerTask.cancel(true);
        stopAudio();
    }

    public void pause()
    {//The cancel call won't kill the loop. It waits for it to end. stopAudio() shuts down audio but the loop is kept in background.
        //We need to end the loop, then close the thread.
        Metronome.setIsRunning(false);
        //clickerTask.cancel(true);
        stopAudio();
    }

    public void resume(int inBeat)
    {//resume audio track and clickertask from pause
        beat=inBeat-1;
        clickerTask = new ClickerTask();
        startAudio();//start Audio
        Metronome.setIsRunning(true);
        if(Build.VERSION.SDK_INT >= 11)
            clickerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            clickerTask.execute();

    }

    protected void startAudio()
    {
        /*Audio init*/
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 2*this.sampleRate, AudioTrack.MODE_STREAM); /**< Buffer is set twice the size of sampleRate so the loop will always be feeding the buffer with 2sec buffered */
        audioTrack.play();
    }

    protected void stopAudio()
    {
        audioTrack.stop();
        audioTrack.release();
    }

    static void setIsRunning (boolean isRunning)
    {
        Metronome.isRunning = isRunning;
    }

    /** Actual worker typically used through an android Task */
    protected void run()
    {


        /** In order: from most common to useless. Do not change the order without adaptating conditions<br>
         * bar is int > 1 tic+tocs<br>
         * bar is dec > 1 tic+tocs+partial toc<br>
         * bar = 1 tic<br>
         * bar = 0 toc<br>
         * bar ]0,1[ stupid partial tic
         */

        if (bar%1 == 0. && bar != 0)//bar is "int". Common use.
            while(isRunning) {
                beat %= (int) bar;//Find which beat is played and Avoid possible int flow.
                //Log.w("metronome_clicker ","beat"+beat);
                if (beat == 0) {
                    //Log.w("metronome.clicker: ","beat"+beat);
                    audioTrack.write(tic,0,tic.length);
                }
                else {
                    //Log.w("metronome.clicker: ","beat"+beat);
                    audioTrack.write(toc,0,toc.length);
                }
                beat++;
                Log.w("metronome.clicker: ","beat"+beat);
            }
        else if (bar > 1.) { //bar is dec >1
            //When beat hits 0 we need to play the partial toc then the tic. This means that the tic will only be played after a partial toc so before we enter the loop, we need to play a tic.
            int partial = (int) ((toc.length)*(bar%1));
            audioTrack.write(tic,0,tic.length);
            beat++;
            while(isRunning) {
                beat %= (int) bar;
                if (beat != 0)//Common beat
                    audioTrack.write(toc,0,toc.length);
                else {//Partial last beat + 1st beat
                    audioTrack.write(toc,0,partial);
                    audioTrack.write(tic,0,tic.length);
                }
                beat++;
            }
        }
        else if (bar == 1.) //Play only tic
            while(isRunning) audioTrack.write(tic,0,tic.length);
        else if (bar == 0.) //Play only toc
            while(isRunning) audioTrack.write(toc,0,toc.length);
        else { //Partial tic. Stupid.
            int partial = (int) ((tic.length)*bar);
            while(isRunning) audioTrack.write(tic,0,partial);
        }
    }
}
