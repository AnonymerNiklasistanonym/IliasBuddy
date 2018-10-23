package com.niklasm.iliasbuddy.memory_managment_api;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.Serializable;

interface IMemoryManagementApi {

    @NonNull
    <T extends Serializable> T getCache(@NonNull final String directory, @NonNull final String fileName)
            throws IOException, ClassNotFoundException;

    <T extends Serializable> void saveToCache(@NonNull final String directory, @NonNull final String fileName,
                                              @NonNull final T data) throws IOException;

    void saveToPersistent(@NonNull final String directory, @NonNull final String fileName,
                          @NonNull final Serializable data);

    @NonNull
    Serializable getPersistent(@NonNull final String directory, @NonNull final String fileName);
}
