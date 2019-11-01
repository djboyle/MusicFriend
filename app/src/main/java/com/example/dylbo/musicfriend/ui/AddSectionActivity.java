package com.example.dylbo.musicfriend.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.dylbo.musicfriend.R;
import com.example.dylbo.musicfriend.Utils.AddSectionViewModel;
import com.example.dylbo.musicfriend.Utils.AddSectionViewModelFactory;
import com.example.dylbo.musicfriend.Utils.AppExecutors;
import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SectionEntry;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.Date;

public class AddSectionActivity extends AppCompatActivity {


        // Extra for the task ID to be received in the intent
        public static final String EXTRA_SECTION_ID = "extraSectionId";
        public static final String EXTRA_SONG_ID = "extraSongId";

        // Extra for the task ID to be received after rotation
        public static final String INSTANCE_SECTION_ID = "instanceSectionId";
        public int SECTION_COLOR = R.color.colorSectionDefault ;

        // Constant for default task id to be used when not in update mode
        private static final int DEFAULT_SECTION_ID = -1;
        private static final int DEFAULT_SONG_ID = -1;

        private int mSectionNumber;
        // Constant for logging
        private static final String TAG = AddSectionActivity.class.getSimpleName();
        // Fields for views
        EditText mEditTextTitle;
        EditText mEditTextNumBars;
        EditText mEditTextSectionDescription;
        Button mButtonDoneSection;
        Button mButtonColor;

        private int mSectionId = DEFAULT_SECTION_ID;
        private int mSongId = DEFAULT_SONG_ID;


        // Member variable for the Database
        private AppDatabase mDb;

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_section);

            initViews();

            mDb = AppDatabase.getInstance(getApplicationContext());

            if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_SECTION_ID)) {
                mSectionId = savedInstanceState.getInt(INSTANCE_SECTION_ID, DEFAULT_SECTION_ID);
            }

            Intent intent = getIntent();
            if (intent != null ) {
                Bundle bundle;
                bundle = intent.getExtras();

                //mButton.setText(R.string.update_button);
                if (mSectionId == DEFAULT_SECTION_ID) {
                    // populate the UI
                    mSectionId = bundle.getInt(EXTRA_SECTION_ID, DEFAULT_SECTION_ID);
                    mSongId = bundle.getInt(EXTRA_SONG_ID, DEFAULT_SONG_ID);
                    // COMPLETED (9) Remove the logging and the call to loadTaskById, this is done in the ViewModel now
                    // COMPLETED (10) Declare a AddTaskViewModelFactory using mDb and mTaskId
                    AddSectionViewModelFactory factory = new AddSectionViewModelFactory(mDb, mSectionId);
                    // COMPLETED (11) Declare a AddTaskViewModel variable and initialize it by calling ViewModelProviders.of
                    // for that use the factory created above AddTaskViewModel
                    final AddSectionViewModel viewModel
                            = ViewModelProviders.of(this, factory).get(AddSectionViewModel.class);

                    // COMPLETED (12) Observe the LiveData object in the ViewModel. Use it also when removing the observer
                    viewModel.getSection().observe(this, new Observer<SectionEntry>() {
                        @Override
                        public void onChanged(@Nullable SectionEntry sectionEntry) {
                            viewModel.getSection().removeObserver(this);
                            populateUI(sectionEntry);
                        }
                    });
                }
            }
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            outState.putInt(INSTANCE_SECTION_ID, mSectionId);
            super.onSaveInstanceState(outState);
        }

        /**
         * initViews is called from onCreate to init the member variable views
         */
        private void initViews() {
            mEditTextTitle = findViewById(R.id.section_enter_title_ET);
            mEditTextNumBars = findViewById(R.id.editTextNumBars);
            mEditTextSectionDescription = findViewById(R.id.editTextSectionDescription);

            mButtonDoneSection = findViewById(R.id.buttonDoneSection);
            mButtonDoneSection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSaveButtonClicked();
                }
            });
            mButtonColor = findViewById(R.id.buttonColor);
            mButtonColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onColorButtonClicked();
                }
            });
        }

        /**
         * populateUI would be called to populate the UI when in update mode
         *
         * @param section the taskEntry to populate the UI
         */
        private void populateUI(SectionEntry section) {
            if (section == null) {
                return;
            }
            mEditTextNumBars.setText(section.getBars());
            mEditTextSectionDescription.setText(section.getDescription());
            mButtonColor.setBackgroundColor(section.getColor());
            mEditTextTitle.setText(section.getSectionTitle());
            SECTION_COLOR = section.getColor();
        }

        /**
         * onSaveButtonClicked is called when the "save" button is clicked.
         * It retrieves user input and inserts that new task data into the underlying database.
         */
        public void onSaveButtonClicked() {
            String description = mEditTextSectionDescription.getText().toString();
            String bars = mEditTextNumBars.getText().toString();
            String title = mEditTextTitle.getText().toString();
            int color = SECTION_COLOR;
            Date date = new Date();

            final SectionEntry section = new SectionEntry(mSongId,title, bars,description,color,100, date);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    if (mSectionId == DEFAULT_SECTION_ID) {
                        // insert new task
                        mDb.getSectionDao().insertSection(section);
                    } else {
                        //update task
                        section.setSectionId(mSectionId);
                        mDb.getSectionDao().updateSection(section);
                    }
                    finish();
                }
            });
        }

    public void onColorButtonClicked() {
        ColorPickerDialogBuilder
                .with(this,R.style.ThemeOverlay_AppCompat_Dark)
                .setTitle("Choose color")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .noSliders()
                .density(8)
                .setOnColorSelectedListener(new OnColorSelectedListener() {

                    @Override
                    public void onColorSelected(int selectedColor) {
                        //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        mButtonColor.setBackgroundColor(selectedColor);
                        SECTION_COLOR=selectedColor;
                        //changeBackgroundColor(selectedColor);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();



    }

        /**
         * getPriority is called whenever the selected priority needs to be retrieved
         */

    }
