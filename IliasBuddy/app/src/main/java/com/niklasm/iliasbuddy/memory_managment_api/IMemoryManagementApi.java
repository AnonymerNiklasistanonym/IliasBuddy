package com.niklasm.iliasbuddy.memory_managment_api;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.Serializable;

interface IMemoryManagementApi {

    void saveToCache(@NonNull final String directory, @NonNull final String fileName,
                     @NonNull final Serializable data) throws IOException;

    @NonNull
    Serializable getCache(@NonNull final String directory, @NonNull final String fileName)
            throws IOException, ClassNotFoundException;

    void saveToPersistent(@NonNull final String directory, @NonNull final String fileName,
                          @NonNull final Serializable data);

    @NonNull
    Serializable getPersistent(@NonNull final String directory, @NonNull final String fileName);
}
