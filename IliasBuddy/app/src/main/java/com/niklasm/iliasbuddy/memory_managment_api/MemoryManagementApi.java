package com.niklasm.iliasbuddy.memory_managment_api;

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
import java.io.Serializable;

public class MemoryManagementApi implements IMemoryManagementApi {

    private final Context context;

    public MemoryManagementApi(@NonNull final Context context) {
        this.context = context;
    }

    private File getCacheFile(@NonNull final String directory, @NonNull final String fileName) {
        return new File(context.getFilesDir().getAbsolutePath() +
                File.separator + directory + File.separator + fileName);
    }

    @NonNull
    @Override
    public <T extends Serializable> T getCache(@NonNull final String directory, @NonNull final String fileName) throws IOException, ClassNotFoundException {
        final File file = getCacheFile(directory, fileName);
        final ObjectInputStream cacheInputStream = new ObjectInputStream(new FileInputStream(file));
        @SuppressWarnings("unchecked")
        final T serializable = (T) cacheInputStream.readObject();
        cacheInputStream.close();
        return serializable;
    }

    @Override
    public <T extends Serializable> void saveToCache(@NonNull final String directory, @NonNull final String fileName, @NonNull final T data) throws IOException {
        final File file = getCacheFile(directory, fileName);
        // check if the parent directory of the cache file exists and if not create it
        if (file.getParentFile().mkdirs()) {
            Log.d("IliasBuddyCacheHandler", "Directory for cache file was created");
        }
        // check if the cache file exists and if not create it
        if (file.createNewFile()) {
            Log.d("IliasBuddyCacheHandler", "Cache file was created");
        }
        final ObjectOutput CACHE_FILE_OUTPUT_STREAM = new ObjectOutputStream(new FileOutputStream(file));
        CACHE_FILE_OUTPUT_STREAM.writeObject(data);
        CACHE_FILE_OUTPUT_STREAM.close();
    }

    @Override
    public void saveToPersistent(@NonNull final String directory, @NonNull final String fileName, @NonNull final Serializable data) {
        // TODO Use sqlite database
    }

    @NonNull
    @Override
    public Serializable getPersistent(@NonNull final String directory, @NonNull final String fileName) {
        // TODO Use sqlite database
        return 0;
    }
}
