package com.niklasm.iliasbuddy.private_rss_feed_api.feed_entry;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.Comparator;

public interface IIliasRssEntry extends Comparator<IliasRssEntry>, Serializable, Parcelable {
    String SIMPLE_TIME_DATE_FORMAT = "dd.MM HH:mm";
}
