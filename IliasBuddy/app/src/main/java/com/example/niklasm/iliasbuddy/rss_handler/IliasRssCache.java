package com.example.niklasm.iliasbuddy.rss_handler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Class that can save the IliasRssItem array to a file and read/load it later
 */
public class IliasRssCache {

    /**
     * Name of the cache file
     */
    private final static String NAME_FILE = "current_items";
    /**
     * Name of the cache file parent directory
     */
    private final static String NAME_DIR = "ilias_rss_cache";

    /**
     * Get cache file
     *
     * @param CONTEXT Needed to access file directory
     * @return Cache file
     */
    @NonNull
    private static File getFile(@NonNull final Context CONTEXT) {
        return new File(CONTEXT.getFilesDir().getAbsolutePath() +
                File.separator + IliasRssCache.NAME_DIR +
                File.separator + IliasRssCache.NAME_FILE);
    }

    /**
     * Read cached entries
     *
     * @param CONTEXT Needed to access file directory
     * @return Cached array with data
     * @throws IOException            Cache file could not be found/read error
     * @throws ClassNotFoundException Object could not be read from serialized data
     */
    @NonNull
    public static IliasRssItem[] getCache(@NonNull final Context CONTEXT)
            throws IOException, ClassNotFoundException {

        // get cache file
        final File ILIAS_RSS_CACHE_FILE = IliasRssCache.getFile(CONTEXT);

        // convert content of cache file to a IliasRssItem array
        final ObjectInputStream CACHE_FILE_INPUT_STREAM =
                new ObjectInputStream(new FileInputStream(ILIAS_RSS_CACHE_FILE));
        final IliasRssItem[] CACHED_ILIAS_RSS_ITEMS =
                (IliasRssItem[]) CACHE_FILE_INPUT_STREAM.readObject();
        CACHE_FILE_INPUT_STREAM.close();

        // return the cached IliasRssItem array
        return CACHED_ILIAS_RSS_ITEMS;
    }

    /**
     * Write entries to cache
     *
     * @param CONTEXT       Needed to access file directory
     * @param DATA_TO_CACHE Array with data that should be cached
     * @throws IOException Cache file could not be found/created/written error
     */
    public static void setCache(@NonNull final Context CONTEXT,
                                @NonNull final IliasRssItem[] DATA_TO_CACHE)
            throws IOException {

        // get cache file
        final File ILIAS_RSS_CACHE_FILE = IliasRssCache.getFile(CONTEXT);

        // check if the parent directory of the cache file exists and if not create it
        if (!ILIAS_RSS_CACHE_FILE.getParentFile().mkdirs()) {
            Log.e("IliasRssCache", "Directory for cache file could not be created");
        }

        // check if the cache file exists and if not create it
        if (!ILIAS_RSS_CACHE_FILE.createNewFile()) {
            Log.e("IliasRssCache", "Directory for cache file could not be created");
        }

        // write given IliasRssItem[] to cache file
        final ObjectOutput CACHE_FILE_OUTPUT_STREAM =
                new ObjectOutputStream(new FileOutputStream(ILIAS_RSS_CACHE_FILE));
        CACHE_FILE_OUTPUT_STREAM.writeObject(DATA_TO_CACHE);
        CACHE_FILE_OUTPUT_STREAM.close();
    }

    public static void clearCache(@NonNull final Context CONTEXT)
            throws IOException {
        IliasRssCache.setCache(CONTEXT, new IliasRssItem[0]);
    }

}
