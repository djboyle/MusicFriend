package com.example.dylbo.musicfriend.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dylbo.musicfriend.R;
import com.example.dylbo.musicfriend.Utils.AppExecutors;
import com.example.dylbo.musicfriend.database.AppDatabase;
import com.example.dylbo.musicfriend.database.SectionEntry;
import com.example.dylbo.musicfriend.database.SongEntry;
import com.example.dylbo.musicfriend.ui.FragmentSongRecordings;

import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.RecordingsViewHolder>{

    private static final String TAG = RecordingsAdapter.class.getSimpleName();
    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    final private RecordingsAdapter.ItemClickListener mItemClickListener;
    final private RecordingsAdapter.ItemLongClickListener mItemLongClickListener;
    private int mActiveRecording =0;

    private AppDatabase mDb;//Database
    private int mSongID;
    private int color;
    private Context mContext;
    // Class variables for the List that holds task data and the Context
    private ArrayList<String> mSongRecordings;

    /**
     * Constructor for GreenAdapter that accepts a number of items to display and the specification
     * for the ListItemClickListener.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    public RecordingsAdapter(Context context, ItemClickListener listener, ItemLongClickListener longClickListener) {
        mContext = context;
        mItemClickListener = listener;
        mItemLongClickListener = longClickListener;
        mDb = AppDatabase.getInstance(mContext);//Get instance of Database

    }


    @Override
    public RecordingsViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Log.d(TAG, "heightparent: " + parent);
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.recycler_item_recordings;

        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(layoutIdForListItem, parent, false);

        RecordingsViewHolder viewHolder = new RecordingsViewHolder(view);

        return viewHolder;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(RecordingsViewHolder holder, final int position) {

        File f = new File(mSongRecordings.get(position));
        // load data file

        String RecordingName = f.getName();
        holder.recordingsFilenameTV.setText(RecordingName);

        //GEt song duration

        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(mSongRecordings.get(position));

        // convert duration to minute:seconds
        String duration =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.v("time", duration);

        List<String> durationMMSS = durationToTimeString(duration);
        String durationTypeString = durationMMSS.get(0) + ":" + durationMMSS.get(1) + " | " + "Session Recording";
        holder.recordingsDurationTypeTV.setText(durationTypeString);
        if(mActiveRecording==position){
            Log.d(TAG, "color: " + color);
            holder.recordingsLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorAccent));
            holder.recordingsFilenameTV.setTextColor(ContextCompat.getColor(mContext,R.color.colorBlack));
            holder.recordingOptionsIB.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorAccent));
            holder.recordingOptionsIB.setImageResource(R.drawable.ic_action_menu_light);
        }else{
            holder.recordingsLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
            holder.recordingsFilenameTV.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
            holder.recordingOptionsIB.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
            holder.recordingOptionsIB.setImageResource(R.drawable.ic_action_menu_dark);
        }

        holder.recordingOptionsIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(mContext, view);
                //inflating menu from xml resource
                popup.inflate(R.menu.recycler_options);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.Delete:
                                File file = new File(mSongRecordings.get(position));
                                boolean deleted = file.delete();
                                Log.d(TAG, "deleted: " + deleted);
                                mSongRecordings.remove(position);
                                notifyDataSetChanged();
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        SongEntry songEntry = mDb.getSongDao().LoadSong(mSongID);
                                        songEntry.setRecordingFileLocations(mSongRecordings);
                                        mDb.getSongDao().updateSong(songEntry);
                                    }
                                });
                                return true;
                            case R.id.Rename:
                                //handle menu2 click
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext,R.style.MyAlertDialogStyle);
                                AlertDialog dialog;
                                builder.setTitle("Rename Recording");

                                View viewInflated = LayoutInflater.from(mContext).inflate(R.layout.dialogue_add_title,null, false);
                                // Set up the input
                                final EditText input = viewInflated.findViewById(R.id.input);
                                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                builder.setView(viewInflated);

                                // Set up the buttons
                                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String mRecordingFilename = input.getText().toString();
                                        String mFileLocation = mContext.getFilesDir().getAbsolutePath();
                                        String filePath =mFileLocation+"/"+mRecordingFilename+".3gp";
                                        //Log.d(TAG, "mRecordingFilename" + filePath);
                                        //Log.d(TAG, "mRecordingLocations" + mRecordingLocations);

                                        if (mSongRecordings.contains(filePath)){
                                            Toast.makeText(mContext, "Filename Already Exists",
                                                    Toast.LENGTH_LONG).show();

                                        }else {
                                            dialog.dismiss();
                                            int check =mRecordingFilename.length();

                                            if(check!=0){
                                                renameRecordingFile(mRecordingFilename, position);
                                                Log.d(TAG, "mRecordingFilename" + mRecordingFilename);
                                                //mSongRecordings.remove(position);
                                                //mSongRecordings.add(position,mRecordingFilename);
                                                //notifyDataSetChanged();
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

                                return true;
                            default:
                                return false;
                        }
                    }
                });
                //displaying the popup
                popup.show();

            }
        });


    }
    public ArrayList<String> getSections() {
        return mSongRecordings;
    }
    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mSongRecordings == null) {
            return 0;
        }
        return mSongRecordings.size();
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setRecordingsLocation(ArrayList<String> songRecorgins, int songID) {
        mSongRecordings = songRecorgins;
        mSongID = songID;
        Log.d(TAG, "setTasks: " + songRecorgins);

        notifyDataSetChanged();
    }
    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setmActiveRecording(int activeRecording) {
        mActiveRecording = activeRecording;
        notifyDataSetChanged();
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public ArrayList<String> getRecordingsList() {
        return mSongRecordings;
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    public interface ItemLongClickListener {
        void onItemLongClicked(int itemId);
    }
    // COMPLETED (5) Implement OnClickListener in the NumberViewHolder class
    /**
     * Cache of the children views for a list item.
     */
    class RecordingsViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView recordingsFilenameTV;
        TextView recordingsDurationTypeTV;
        FrameLayout recordingsLayout;
        ImageButton recordingOptionsIB;
        //ProgressBar sectionProgressBar;
        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         *
         * @param itemView The View that you inflated in
         *                 {@link SectionAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public RecordingsViewHolder(View itemView) {
            super(itemView);

            recordingsFilenameTV = itemView.findViewById(R.id.recording_filename_TV);
            recordingsDurationTypeTV=itemView.findViewById(R.id.duration_type_TV);
            recordingsLayout = itemView.findViewById(R.id.record_recycler_item);
            recordingOptionsIB= itemView.findViewById(R.id.recording_options_IB);
            // COMPLETED (7) Call setOnClickListener on the View passed into the constructor (use 'this' as the OnClickListener)
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        // COMPLETED (6) Override onClick, passing the clicked item's position (getAdapterPosition()) to mOnClickListener via its onListItemClick method
        /**
         * Called whenever a user clicks on an item in the list.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {

            int elementId = getAdapterPosition();
            mItemClickListener.onItemClickListener(elementId);
        }

        @Override
        public boolean onLongClick(View v) {
            //int elementId = getAdapterPosition();
            //mItemLongClickListener.onItemLongClicked(elementId);
            return true;
        }
    }
    public List<String> durationToTimeString(String duration){
        //List<String> out= new List<String>;
        Log.v("time", duration);
        long dur = Long.parseLong(duration);
        String seconds = String.valueOf((dur % 60000) / 1000);

        Log.v("seconds", seconds);
        String minutes = String.valueOf(dur / 60000);
        //out = minutes + ":" + seconds;
        if (seconds.length() == 1) {
            seconds = ("0" + seconds);
        }if (minutes.length() ==1){
            minutes = ("0" + minutes);
        }
        Log.v("minutes", minutes);
        // close object
        List<String> minSec = Arrays.asList(minutes, seconds);
        return minSec;
    }

    private void renameRecordingFile(String newFileName, final int position){
        final File recording = new File(mSongRecordings.get(position));
        String directory = recording.getParent();
        final File newName = new File(directory,"/" +newFileName + ".3gp");
        if(recording.renameTo(newName)){
            System.out.println("Succes! Name changed to: " + recording.getName());
        }else{
            System.out.println("failed");
        }
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                SongEntry songEntry = mDb.getSongDao().LoadSong(mSongID);
                //Date date = new Date();
                ArrayList<String> recordings = songEntry.getRecordingFileLocations();
                Log.d(TAG, "recordings Array: "+ recordings);
                Log.d(TAG, "filename: "+ newName.getPath());
                recordings.remove(position);
                recordings.add(position,newName.getPath());
                songEntry.setRecordingFileLocations(recordings);
                mDb.getSongDao().updateSong(songEntry);

            }
        });
    }
}


