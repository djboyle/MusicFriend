package com.example.dylbo.musicfriend.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dylbo.musicfriend.R;
import com.example.dylbo.musicfriend.Utils.AppExecutors;
import com.example.dylbo.musicfriend.Utils.MainViewModel;
import com.example.dylbo.musicfriend.adapters.SectionAdapter;
import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SectionEntry;

import java.util.List;

public class FragmentSongSections extends Fragment
        implements SectionAdapter.ItemClickListener, SectionAdapter.ItemLongClickListener{
    // Constant for logging
    private static final String TAG = FragmentSongSections.class.getSimpleName();
    // Extra for the task ID to be received in the intent
    public static final String EXTRA_SONG_ID = "extraSongId";
    public static final String EXTRA_SONG_TITLE = "extraSongTitle";
    //Define views and adapter
    private RecyclerView mSectionRV;
    private SectionAdapter mSectionAdapter;
    private AppDatabase mDb;//Database
    private int mSongID;
    private String mSongTitle;
    private List<SectionEntry> mSections;


    private TextView mSongBannerTitleTV;
    private MaterialButton mPlayButton;
    //Mandatory constructor used for instantiating the fragment
    public FragmentSongSections() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the song sections fragment layout
        View rootView = inflater.inflate(R.layout.fragment_song_sections, container, false);
        /* THIS WILL MOVE TO FRAGMENT??
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.*/
        mSongID = getArguments().getInt(EXTRA_SONG_ID);
        mSongTitle = getArguments().getString(EXTRA_SONG_TITLE);
        mSectionRV = rootView.findViewById(R.id.rv_song_section);

        //Set up linear manager for recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mSectionRV.setLayoutManager(layoutManager);
        mSectionRV.setItemAnimator(null);


        //Attache adapter
        mSectionAdapter = new SectionAdapter(getActivity(), this, this);
        mSectionRV.setAdapter(mSectionAdapter);

        //Call recycler view adapter

        mDb = AppDatabase.getInstance(getActivity());//Get instance of Database

        mSongBannerTitleTV = rootView.findViewById(R.id.song_title_TV);

        mSongBannerTitleTV.setText(mSongTitle);//set activity title
        //////////////////////FAB button setup////////////////////
         /*Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */

        final FloatingActionButton fabButton = rootView.findViewById(R.id.add_section_FAB);//Play button FAB

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch AddTaskActivity adding the song id as an extra in the intent
                Intent intent = new Intent(getActivity(), AddSectionActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putInt(AddSectionActivity.EXTRA_SONG_ID, mSongID);
                intent.putExtras(mBundle);
                startActivity(intent);

            }
        });
        //////////////////////Play button setup////////////////////

        mPlayButton = rootView.findViewById(R.id.play_icon_IB);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch PlaySongActivity
                Intent intent = new Intent(getActivity(), PlaySongActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString(SongActivity.EXTRA_SONG_TITLE, mSongTitle);
                mBundle.putInt(SongActivity.EXTRA_SONG_ID, mSongID);
                intent.putExtras(mBundle);
                startActivity(intent);
            }
        });
        setupViewModel();
        return rootView;
    }

    private void setupViewModel() {
        //Setup initial View model
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        //Observe for any changes to song sections

        viewModel.getSongSections(mSongID).observe(this, new Observer<List<SectionEntry>>() {
            @Override
            public void onChanged(@Nullable List<SectionEntry> sectionEntries) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mSectionAdapter.setSections(sectionEntries);//Call set tasks method in adapter
                mSections = sectionEntries;
            }

        });

    }
    @Override
    public void onItemClickListener(int position) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(getActivity(), AddSectionActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putInt(AddSectionActivity.EXTRA_SECTION_ID, position);
        mBundle.putInt(AddSectionActivity.EXTRA_SONG_ID, mSongID);
        intent.putExtras(mBundle);
        startActivity(intent);
    }
    @Override
    public void onItemLongClicked(final int position) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<SectionEntry> sections = mSectionAdapter.getSections();
                mDb.getSectionDao().deleteSection(sections.get(position));
            }
        });
    }

}