package com.example.dylbo.musicfriend.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dylbo.musicfriend.R;
import com.example.dylbo.musicfriend.database.SectionEntry;

import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {

    private static final String TAG = SectionAdapter.class.getSimpleName();
    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    final private ItemClickListener mItemClickListener;
    final private ItemLongClickListener mItemLongClickListener;


    private Context mContext;
    // Class variables for the List that holds task data and the Context
    private List<SectionEntry> mSectionEntries;

    /**
     * Constructor for GreenAdapter that accepts a number of items to display and the specification
     * for the ListItemClickListener.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    public SectionAdapter(Context context, ItemClickListener listener, ItemLongClickListener longClickListener) {
        mContext = context;
        mItemClickListener = listener;
        mItemLongClickListener = longClickListener;

    }





    @Override
    public SectionViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Log.d(TAG, "heightparent: " + parent);
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.recycler_item_section;

        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(layoutIdForListItem, parent, false);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        //Log.d(TAG, "heighttotal: " + parent.getHeight());
        layoutParams.height = 372;
        Log.d(TAG, "height: " + layoutParams.height);
        view.setLayoutParams(layoutParams);
        /*
        view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        // make sure it is not called anymore
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                        Log.d(TAG, "heighttotal: " + parent.getHeight());
                        layoutParams.height = (int) (parent.getHeight()*0.25);
                        Log.d(TAG, "height: " + layoutParams.height);
                        view.setLayoutParams(layoutParams);
                        // your code to get dimensions of the view here:
                        // ...
                    }
                });
        */
        //Set height of recycler view to 1/4 screen size
        SectionViewHolder viewHolder = new SectionViewHolder(view);

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
    public void onBindViewHolder(SectionViewHolder holder, int position) {

        // Determine the values of the wanted data
        SectionEntry sectionEntry = mSectionEntries.get(position);
        String description = sectionEntry.getDescription();
        String sectionTitle = sectionEntry.getSectionTitle();
        //String  TITLE
        int color = sectionEntry.getColor();
        String bars = sectionEntry.getBars();
        Log.d(TAG, "sectionEntry: " + sectionEntry);
        //Set values
        String barsText = bars + " Bars";
        holder.sectionTitleTV.setText(sectionTitle);
        holder.sectionDescriptionTV.setText(description);
        holder.sectionBarsTV.setText(barsText);
        holder.sectionSideColorIV.setBackgroundColor(color);
        holder.seperationBarIV.setBackgroundColor(color);

    }
    public List<SectionEntry> getSections() {
        return mSectionEntries;
    }
    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mSectionEntries == null) {
            return 0;
        }
        return mSectionEntries.size();
    }
    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setSections(List<SectionEntry> sectionEntries) {
        mSectionEntries = sectionEntries;
        Log.d(TAG, "setTasks: " + sectionEntries);
        notifyDataSetChanged();
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
    class SectionViewHolder extends RecyclerView.ViewHolder
            implements OnClickListener, View.OnLongClickListener {

        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView sectionTitleTV;
        TextView sectionDescriptionTV;
        TextView sectionBarsTV;
        ImageView sectionSideColorIV;
        ImageView seperationBarIV;
        //ProgressBar sectionProgressBar;
        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         *
         * @param itemView The View that you inflated in
         *                 {@link SectionAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public SectionViewHolder(View itemView) {
            super(itemView);

            sectionDescriptionTV = itemView.findViewById(R.id.section_description_TV);
            sectionBarsTV = itemView.findViewById(R.id.section_bars_TV);
            sectionTitleTV = itemView.findViewById(R.id.section_title_TV);
            sectionSideColorIV = itemView.findViewById(R.id.side_color_bar_IV);
            seperationBarIV = itemView.findViewById(R.id.seperation_color_bar_IV);
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
            int elementId = mSectionEntries.get(getAdapterPosition()).getSectionID();
            mItemClickListener.onItemClickListener(elementId);
        }

        @Override
        public boolean onLongClick(View v) {
            int elementId = getAdapterPosition();
            mItemLongClickListener.onItemLongClicked(elementId);
            return true;
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }
}
