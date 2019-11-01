package com.example.dylbo.musicfriend.Utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.dylbo.musicfriend.database.SectionEntry;
import com.example.dylbo.musicfriend.ui.PlaySongActivity;
import com.example.dylbo.musicfriend.ui.PlayingNextFragment;
import com.example.dylbo.musicfriend.ui.UpcomingFragment;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
///////////////////////////////////////////////////////////////////////////
//Variables parsed

//Class to handle playing songs
public class RunSong {
    ///////////////////////////////////////////////////////////////////////////
    //Variables parsed
    protected int mSongID;
    protected Context context;
    protected List<SectionEntry> songSections;
    protected PlayingNextFragment playingFragment;
    protected PlayingNextFragment nextFragment;
    protected UpcomingFragment upcomingFragment1;
    protected UpcomingFragment upcomingFragment2;
    protected PlaySongActivity playSongActivity;
    ///////////////////////////////////////////////////////////////////////////
    //Internal variables
    private Metronome metronome;//Instansiation of metronome
    private int timeSig = 4;//time signature
    private float BPM = 110;//Song Beats Per Minute
    private int mSectionCounter;
    private int BeatMilli = Math.round((60 / BPM) * 1000);
    private Timer timer; //Timer used in play function
    private boolean newStart = TRUE;//flag for song paused
    private boolean playing = FALSE;//flag for song playing
    private boolean resume = FALSE;//flag set when resumed from pause
    private int[] barsArray = new int[50];//Arry holding section bars
    private float[] sectionSeconds = new float[50];//Array holding seconds in each section
    private final int[] sectionBeats = new int[50];//Array holding beats in each section
    private int time_counter = 0;  // Seconds to set the Countdown from;
    private int section_counter = 0;//Section counter
    private int mNumSections = 0;//Total number of sections
    private int beatCounter=1;

    public RunSong(int songID, List<SectionEntry> songSections, Context context, PlayingNextFragment playingFragment,
                   PlayingNextFragment nextFragment, UpcomingFragment upcomingFragment1, UpcomingFragment upcomingFragment2, PlaySongActivity playSongActivity) {
        this.mSongID = songID;
        this.context = context;
        this.songSections = songSections;
        this.playingFragment = playingFragment;
        this.nextFragment = nextFragment;
        this.upcomingFragment1 = upcomingFragment1;
        this.upcomingFragment2 = upcomingFragment2;
        this.playSongActivity = playSongActivity;

    }

    public void run(){
        //Set playing flag for
        if (newStart) {
            int sectionsSize = songSections.size();
            for (int i = 0; i < sectionsSize; i++) {
                // mSectionAdapter.setProgArray(0, i, sectionBeats[i]);
            }
            mSectionCounter = 0;
            mNumSections=0;
            //Get all bars from each section
            for (int i = 0; i < sectionsSize; i++) {
                String bars = songSections.get(i).getBars();
                if (!bars.equals("")) {
                    barsArray[i] = Integer.parseInt(bars);
                    Log.d(TAG, "log bars array=" + barsArray[i]);
                    sectionBeats[i] = barsArray[i] * timeSig;
                    sectionSeconds[i] = 60 * sectionBeats[i] / BPM;
                    mNumSections++;
                    Log.d(TAG, "sections calc =" + mNumSections);
                }
            }
            //playing = TRUE;
        }
        try {
            if(newStart) {
                metronome = new Metronome(BPM, timeSig, context);//if starting for first time
            }
        } catch (OutOfMemoryError e) {
            Toast.makeText(context, "out of mem", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }


        timer = new Timer();        // A thread of execution is instantiated
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //resume from pause state
                if (playing){
                    if (newStart){
                        metronome.start();  //Start Metronome
                        newStart=FALSE;
                    }else if (resume) {
                        metronome.resume(beatCounter);
                        resume=FALSE;
                        Log.d(TAG, "check section resume=" );
                    }


                    time_counter++;
                    beatCounter++;
                    Log.d(TAG, "metronome.clicker=" + beatCounter);
                    if(beatCounter==5) beatCounter=1;

                    //Update progress bar
                    if(time_counter==1){ playingFragment.setProgressBarMax(sectionBeats[section_counter]);}
                    playingFragment.setProgressBar(time_counter);


                    //This may not run properly!!
                    //mSectionAdapter.setProgArray(time_counter, section_counter, sectionBeats[section_counter]);

                    if (time_counter == sectionBeats[section_counter]) {

                        bumpUpSections(section_counter);
                        time_counter = 0;
                        mSectionCounter++;
                        if (mSectionCounter == mNumSections) {
                            mSectionCounter = 0;
                            playing = FALSE;

                            newStart=TRUE;
                            //fabButtonReplay.show();
                            timer.cancel(); // timer is cancelled when time reaches 0
                            metronome.stop();
                            playSongActivity.stop();
                        }
                    }
                }else timer.cancel();
            }
        }, 0, BeatMilli);
        // 0 is the time in second from when this code is to be executed
        // 1000 is time in millisecond after which it has to repeat
    }
    private void bumpUpSections (int currentSection){
        int size = songSections.size();
        Log.d(TAG, "size=" + size);
        Log.d(TAG, "currentSection=" + currentSection);
        if( currentSection +1 <size) {
            playingFragment.setFragmentDetails(songSections.get(currentSection +1).getSectionTitle(), songSections.get(currentSection +1).getBars(),
                    songSections.get(currentSection +1).getDescription(), songSections.get(currentSection +1).getColor());
        }else{
            if (size>0){
                playingFragment.setVisibility(View.GONE);}
        }
        if( currentSection +2 <size) {
            nextFragment.setFragmentDetails(songSections.get(currentSection +2 ).getSectionTitle(), songSections.get(currentSection +2 ).getBars(),
                    songSections.get(currentSection +2 ).getDescription(), songSections.get(currentSection +2 ).getColor());
            nextFragment.setProgressBarMax(10);
            nextFragment.setProgressBar(10);
        }else {
            if (size>1){
                nextFragment.setVisibility(View.GONE);
            }
        }
        if( currentSection +3 <size) {
            upcomingFragment1.setFragmentDetails(songSections.get(currentSection +3).getSectionTitle(), songSections.get(currentSection +3).getBars(),
                    songSections.get(currentSection +3).getColor());
        }else {
            if (size>2){
                upcomingFragment1.setVisibility(View.GONE);
            }
        }
        if( currentSection +4 <size) {
            upcomingFragment2.setFragmentDetails(songSections.get(currentSection +4).getSectionTitle(), songSections.get(currentSection +4).getBars(),
                    songSections.get(currentSection +4).getColor());
        }else {
            if (size>3){
                upcomingFragment2.setVisibility(View.GONE);
            }
        }
    }

    public void resetSections (){
        int size = songSections.size();
        Log.d(TAG, "songSections=" + size);
        int currentSection=0;
        if( currentSection <size) {
            playingFragment.setFragmentDetails(songSections.get(currentSection +1).getSectionTitle(), songSections.get(currentSection ).getBars(),
                    songSections.get(currentSection ).getDescription(), songSections.get(currentSection ).getColor());
            playingFragment.setVisibility(View.VISIBLE);
        }

        if( currentSection +1 <size) {
            nextFragment.setFragmentDetails(songSections.get(currentSection +1 ).getSectionTitle(), songSections.get(currentSection +1 ).getBars(),
                    songSections.get(currentSection +1 ).getDescription(), songSections.get(currentSection +1 ).getColor());
            nextFragment.setProgressBarMax(10);
            nextFragment.setProgressBar(10);
            nextFragment.setVisibility(View.VISIBLE);
        }

        if( currentSection +2 <size) {
            upcomingFragment1.setFragmentDetails(songSections.get(currentSection +2).getSectionTitle(), songSections.get(currentSection +2).getBars(),
                    songSections.get(currentSection +2).getColor());
            upcomingFragment1.setVisibility(View.VISIBLE);
        }

        if( currentSection +3 <size) {
            upcomingFragment2.setFragmentDetails(songSections.get(currentSection +3).getSectionTitle(), songSections.get(currentSection +3).getBars(),
                    songSections.get(currentSection +3).getColor());
            upcomingFragment2.setVisibility(View.VISIBLE);
        }
    }

    public void playPauseSong (){
        if(playing) {
            playing = FALSE;
            //timer.cancel();
            metronome.pause();
        }else {
            playing = TRUE;
            if(!newStart) {
                resume = TRUE;
            }
            //metronome.resume();
        }

    }

    public void restartSong (){
        //Restart section and beats counters
        playing=TRUE;
        section_counter=0;
        time_counter=0;
        beatCounter=1;
        resume = TRUE;

    }

}
