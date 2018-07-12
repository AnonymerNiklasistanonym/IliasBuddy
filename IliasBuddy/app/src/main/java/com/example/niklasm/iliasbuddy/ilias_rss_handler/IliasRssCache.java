package com.example.niklasm.iliasbuddy.ilias_rss_handler;

import android.content.Context;
import android.support.annotation.NonNull;

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
     * The file in which the IliasRssItem array will be saved
     */
    final private File ILIAS_RSS_CACHE_FILE;

    /**
     * Constructor
     *
     * @param context (Context) - Needed for getting the application file directory
     */
    public IliasRssCache(@NonNull final Context context) {
        final File ILIAS_RSS_CACHE_DIRECTORY =
                new File(context.getFilesDir().getAbsolutePath() +
                        File.separator + "ilias_rss_cache");
        ILIAS_RSS_CACHE_FILE =
                new File(ILIAS_RSS_CACHE_DIRECTORY + File.separator + "TestFile.test");
    }

    /**
     * Read cache file if it exists
     *
     * @return Array of cached IliasRssItem entries
     * @throws IOException            If the file could not be read or there were problems while doing so
     * @throws ClassNotFoundException If the object could not be serialized or the wrong object was found
     */
    public IliasRssItem[] getCache() throws IOException, ClassNotFoundException {
        // check if the cache file exists
        if (!ILIAS_RSS_CACHE_FILE.getParentFile().exists()) {
            throw new IliasRssCacheException(
                    "Cache Directory (" + ILIAS_RSS_CACHE_FILE.getParentFile().toString() + ") does not exist!");
        } else if (!ILIAS_RSS_CACHE_FILE.exists()) {
            throw new IliasRssCacheException("Cache File (" + ILIAS_RSS_CACHE_FILE.toString() + ") does not exist!");
        }
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
     * Write IliasRssItem[] to the cache file
     *
     * @param DATA_TO_CACHE (IliasRssItem[]) Array that should be saved
     * @throws IOException If the file could not be written or there were problems while doing so
     */
    public void setCache(final IliasRssItem[] DATA_TO_CACHE) throws IOException {
        // check if the parent directory of the cache file can be created (if it not already exists)
        if (!ILIAS_RSS_CACHE_FILE.getParentFile().exists() && !ILIAS_RSS_CACHE_FILE.getParentFile().mkdirs()) {
            throw new IliasRssCacheException("Cache Directory (" + ILIAS_RSS_CACHE_FILE.getParentFile().toString() + ") could not be created!");
        }
        // write IliasRssItem array to cache file
        final ObjectOutput CACHE_FILE_OUTPUT_STREAM =
                new ObjectOutputStream(new FileOutputStream(ILIAS_RSS_CACHE_FILE));
        CACHE_FILE_OUTPUT_STREAM.writeObject(DATA_TO_CACHE);
        CACHE_FILE_OUTPUT_STREAM.close();
    }

    /**
     * Error thrower for better cases in this class
     */
    final public class IliasRssCacheException extends RuntimeException {
        IliasRssCacheException(final String EXCEPTION_MESSAGE) {
            super("IliasRssCache - " + EXCEPTION_MESSAGE);
        }
    }
}
