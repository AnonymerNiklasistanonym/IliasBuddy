package com.example.niklasm.iliasbuddy.ilias_rss_handler;

import android.view.View;

public interface IliasRssItemListAdapterInterface extends IliasRssItemAlertDialogInterface {

    /**
     * Get the latest Ilias RSS entry (the one before the last reload)
     *
     * @return (IliasRssItem) Cached Ilias RSS entry before reload
     */
    IliasRssItem listAdapterGetLatestEntry();

    /**
     * Get recycler view position to identify which child of the view was clicked
     *
     * @param view (View) The view that was just clicked
     * @return (int) The child view position
     */
    int listAdapterGetRecyclerViewChildLayoutPosition(View view);
}
