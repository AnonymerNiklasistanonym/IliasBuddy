package com.example.niklasm.iliasbuddy.recycler_view;

import android.view.View;

public interface IliasRssItemListAdapterInterface {

    /**
     * Get the latest Ilias RSS entry (the one before the last reload)
     *
     * @return Cached Ilias RSS entry date as time before reload
     */
    long listAdapterGetLatestEntryTime();

    /**
     * Get recycler view position to identify which child of the view was clicked
     *
     * @param view (View) The view that was just clicked
     * @return (int) The child view position
     */
    int listAdapterGetRecyclerViewChildLayoutPosition(View view);

    /**
     * Notify that the filter settings changed
     *
     * @param filterPosts Current state of post filter
     * @param filterFiles Current state of file filter
     */
    void filterChangeCallback(boolean filterPosts, boolean filterFiles);
}
