package com.example.dylbo.musicfriend.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dylbo.musicfriend.R;

import com.example.dylbo.musicfriend.Utils.AppExecutors;
import com.example.dylbo.musicfriend.adapters.SectionAdapter;
import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SectionEntry;


import java.util.List;

public class PlayingNextFragment extends Fragment{
    //General song variables
    private AppDatabase mDb;//Database
    private String mSongTitle;
    private int mSongID;

    //Define views
    TextView titleTextView;
    TextView barsTextView ;
    TextView descriptionTextView;
    ImageView colorBarImageView;
    ProgressBar progressBar ;
    ConstraintLayout constraintLayout;

    //SectionEntry
    List<SectionEntry> mSectionEntry;
    // Extra for the task ID to be received in the intent
    public static final String EXTRA_SECTION_TITLE = "extraSectionTitle";
    public static final String EXTRA_SECTION_DESCRIPTION= "extraSectionDescription";
    public static final String EXTRA_SECTION_COLOR = "extraSectionColor";
    public static final String EXTRA_SECTION_BARS = "extraSectionBars";

        // Constant for logging
    private static final String TAG = PlayingNextFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Inflate the song sections fragment layout
        View rootView = inflater.inflate(R.layout.fragment_playing_next, container, false);

        //Get title, bars and description
        titleTextView = rootView.findViewById(R.id.section_title_PN_TV);
        barsTextView = rootView.findViewById(R.id.section_bars_PN_TV);
        descriptionTextView = rootView.findViewById(R.id.section_description_PN_TV);
        colorBarImageView = rootView.findViewById(R.id.side_color_bar_PN_IV);
        progressBar = rootView.findViewById(R.id.section_progress_PN_PB);
        constraintLayout = rootView.findViewById(R.id.fragment_playing_next);

        //Set section details from parsed arguments
        titleTextView.setText(getArguments().getString(EXTRA_SECTION_TITLE));
        String barsSetText = getArguments().getString(EXTRA_SECTION_BARS) + " Bars";
        barsTextView.setText(barsSetText);
        descriptionTextView.setText(getArguments().getString(EXTRA_SECTION_DESCRIPTION));
        colorBarImageView.setBackgroundColor(getArguments().getInt(EXTRA_SECTION_COLOR));

        progressBar.getProgressDrawable().setColorFilter(getArguments().getInt(EXTRA_SECTION_COLOR), PorterDuff.Mode.SRC_IN);
        progressBar.setProgress(100);

        return rootView;
    }

    // Setter methods for updating section information

    public void setProgressBar(final int progress) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
            }
        });


    }

    public void setProgressBarMax(final int progressMax) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setMax(progressMax);
            }
        });
    }

    public void setFragmentDetails(final String title,final String bars,final String description,final int color ) {
        //Set section details from parsed arguments
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                titleTextView.setText(title);
                String barsSetText = bars + " Bars";
                barsTextView.setText(barsSetText);
                descriptionTextView.setText(description);
                colorBarImageView.setBackgroundColor(color);
                progressBar.setProgress(0);
                progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        });


    }
    public void setVisibility(final int visibility) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                constraintLayout.setVisibility(visibility);
            }
        });
    }

}
