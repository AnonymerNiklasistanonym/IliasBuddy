package com.example.niklasm.iliasbuddy;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class IliasRssDataSaver {

    final File directory;
    final private Context context;
    final private String fileName;

    public IliasRssDataSaver(final Context context, final String fileName) {
        this.context = context;
        this.fileName = fileName;
        this.directory = new File(context.getFilesDir().getAbsolutePath() + File.separator + "serialisation");
    }

    public IliasRssItem[] readRssFeed() {
        try {
            final ObjectInputStream input = new ObjectInputStream(new FileInputStream(this.directory
                    + File.separator + this.fileName));
            final IliasRssItem[] ReturnClass = (IliasRssItem[]) input.readObject();
            input.close();
            return ReturnClass;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void writeRssFeed(IliasRssItem[] saveThisObject) {

        if (!this.directory.exists() && !this.directory.mkdirs()) {
            Log.e("IliasRssDataSaver Error", "Directory (" + this.directory.toString() + "could not be created!");
        }

        try {
            final ObjectOutput out = new ObjectOutputStream(new FileOutputStream(this.directory
                    + File.separator + this.fileName));
            out.writeObject(saveThisObject);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
