package com.example.niklasm.iliasbuddy.ilias_rss_handler;

import android.view.View;

public interface IliasRssItemListAdapterInterface {

    IliasRssItem listAdapterGetLatestEntry();

    void listAdapterOpenUrl(String url);

    int listAdapterGetRecyclerViewChildLayoutPosition(View view);
}
