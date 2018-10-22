package com.niklasm.iliasbuddy.objects;

import android.support.annotation.NonNull;

import com.niklasm.iliasbuddy.private_rss_feed_api.feed_entry.IliasRssEntry;

abstract class IliasRssSavedEntry extends IliasRssEntry {
    @NonNull
    final private String NAME;

    /**
     * Constructor that creates a new IliasEntry
     *
     * @param NAME Name of the Ilias course of the RSS entry ("Math for Informatics")
     */
    IliasRssSavedEntry(@NonNull final String NAME, @NonNull final IliasRssEntry iliasRssEntry) {
        super(iliasRssEntry);
        this.NAME = NAME;
    }

    /**
     * @return Name of saved entry
     */
    @NonNull
    public String getNAME() {
        return NAME;
    }
}
