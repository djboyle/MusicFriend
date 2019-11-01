package com.example.dylbo.musicfriend.ui;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dylbo.musicfriend.R;
import com.example.dylbo.musicfriend.adapters.SectionAdapter;

public class UpcomingFragment extends Fragment{

    TextView titleTextView;
    TextView barsTextView ;
    ImageView colorBarImageView;
    ConstraintLayout constraintLayout;
    // Extra for the task ID to be received in the intent
    public static final String EXTRA_SECTION_TITLE = "extraSectionTitle";
    public static final String EXTRA_SECTION_COLOR = "extraSectionColor";
    public static final String EXTRA_SECTION_BARS = "extraSectionBars";

    // Constant for logging
    private static final String TAG = UpcomingFragment.class.getSimpleName();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Inflate the song sections fragment layout
        View rootView = inflater.inflate(R.layout.fragment_upcoming, container, false);


        //Get title, bars and description
        titleTextView = rootView.findViewById(R.id.section_title_Upcoming_TV);
        barsTextView = rootView.findViewById(R.id.section_bars_Upcoming_TV);
        colorBarImageView = rootView.findViewById(R.id.side_color_bar_Upcoming_IV);
        constraintLayout = rootView.findViewById(R.id.fragment_upcoming);

        //Set section details from parsed arguments
        titleTextView.setText(getArguments().getString(EXTRA_SECTION_TITLE));
        String barsSetText = getArguments().getString(EXTRA_SECTION_BARS) + " Bars";
        barsTextView.setText(barsSetText);
        colorBarImageView.setBackgroundColor(getArguments().getInt(EXTRA_SECTION_COLOR));
        Log.d(TAG, "color: " + getArguments().getInt(EXTRA_SECTION_COLOR));



        return rootView;
    }
    public void setFragmentDetails(final String title,final String bars,final int color ) {
        //Set section details from parsed arguments
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                titleTextView.setText(title);
                String barsSetText = bars + " Bars";
                barsTextView.setText(barsSetText);
                colorBarImageView.setBackgroundColor(color);

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
