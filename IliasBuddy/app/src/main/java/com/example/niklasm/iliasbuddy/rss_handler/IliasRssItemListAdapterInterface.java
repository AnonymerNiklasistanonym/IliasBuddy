package com.example.niklasm.iliasbuddy.rss_handler;

import android.view.View;

import com.example.niklasm.iliasbuddy.objects.IliasRssFeedItem;

public interface IliasRssItemListAdapterInterface extends IliasRssItemAlertDialogInterface {

    /**
     * Get the latest Ilias RSS entry (the one before the last reload)
     *
     * @return (IliasRssFeedItem) Cached Ilias RSS entry before reload
     */
    IliasRssFeedItem listAdapterGetLatestEntry();

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
