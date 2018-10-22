package com.niklasm.iliasbuddy.handler;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.niklasm.iliasbuddy.private_rss_feed_api.feed_entry.IliasRssEntry;

public class IliasBuddyBroadcastHandler {

    public static final String UPDATE_SILENT = "UPDATE_SILENT";
    public static final String ENABLE_SHORTCUT_CAMPUS = "ENABLE_SHORTCUT_CAMPUS";
    public static final String ENABLE_SHORTCUT_SETUP = "ENABLE_SHORTCUT_SETUP";
    public static final String ENABLE_SHORTCUT_DEV = "ENABLE_SHORTCUT_DEV";
    public static final String NEW_ENTRIES_FOUND = "NEW_ENTRIES_FOUND";
    public static final String NEW_ENTRIES_FOUND_PREVIEW = "NEW_ENTRIES_FOUND_PREVIEW";
    public static final String NEW_ENTRIES_DATA = "NEW_ENTRIES_DATA";

    private static void sendBroadcast(@NonNull final Context CONTEXT,
                                      @NonNull final String INTENT_NAME) {
        LocalBroadcastManager.getInstance(CONTEXT).sendBroadcast(new Intent(INTENT_NAME));
    }

    public static void sendBroadcastUpdateSilent(@NonNull final Context CONTEXT) {
        IliasBuddyBroadcastHandler
                .sendBroadcast(CONTEXT, IliasBuddyBroadcastHandler.UPDATE_SILENT);
    }

    public static void sendBroadcastEnableShortcutCampus(@NonNull final Context CONTEXT) {
        IliasBuddyBroadcastHandler
                .sendBroadcast(CONTEXT, IliasBuddyBroadcastHandler.ENABLE_SHORTCUT_CAMPUS);
    }

    public static void sendBroadcastEnableShortcutSetup(@NonNull final Context CONTEXT) {
        IliasBuddyBroadcastHandler
                .sendBroadcast(CONTEXT, IliasBuddyBroadcastHandler.ENABLE_SHORTCUT_SETUP);
    }

    public static void sendBroadcastEnableShortcutDev(@NonNull final Context CONTEXT) {
        IliasBuddyBroadcastHandler
                .sendBroadcast(CONTEXT, IliasBuddyBroadcastHandler.ENABLE_SHORTCUT_DEV);
    }

    public static void sendBroadcastNewEntriesFound(@NonNull final Context CONTEXT,
                                                    @NonNull final String NOTIFICATION_PREVIEW,
                                                    @NonNull final IliasRssEntry[] NEW_ENTRIES) {
        LocalBroadcastManager.getInstance(CONTEXT).sendBroadcast(
                new Intent(IliasBuddyBroadcastHandler.NEW_ENTRIES_FOUND)
                        .putExtra(IliasBuddyBroadcastHandler.NEW_ENTRIES_FOUND_PREVIEW, NOTIFICATION_PREVIEW)
                        .putExtra(IliasBuddyBroadcastHandler.NEW_ENTRIES_DATA, NEW_ENTRIES));
    }
}
