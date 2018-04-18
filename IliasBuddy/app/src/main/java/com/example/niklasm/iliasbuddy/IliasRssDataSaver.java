package com.example.niklasm.iliasbuddy;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * CLass that save IliasRssItem[] to a file and can read it too
 */
public class IliasRssDataSaver {

    final private File directory;
    final private File iliasRssItemFile;

    IliasRssDataSaver(final Context context, final String fileName) {
        this.directory = new File(context.getFilesDir().getAbsolutePath() + File.separator + "serialisation");
        this.iliasRssItemFile = new File(this.directory + File.separator + fileName);
    }

    public IliasRssItem[] readRssFeed() throws IOException, ClassNotFoundException {
        if (!this.directory.exists()) {
            throw new IliasRssDataSaverException("readRssFeed() - Directory (" + this.directory.toString() + ") does not exist!");
        }
        if (!iliasRssItemFile.exists()) {
            throw new IliasRssDataSaverException("readRssFeed() - File (" + iliasRssItemFile.toString() + ") does not exist!");
        }
        final ObjectInputStream input = new ObjectInputStream(new FileInputStream(iliasRssItemFile));
        final IliasRssItem[] readIliasRssItems = (IliasRssItem[]) input.readObject();
        input.close();
        return readIliasRssItems;
    }

    public void writeRssFeed(IliasRssItem[] saveThisObject) throws IOException {
        if (!this.directory.exists() && !this.directory.mkdirs()) {
            throw new IliasRssDataSaverException("writeRssFeed() - Directory (" + this.directory.toString() + ") could not be created!");
        }
        final ObjectOutput out = new ObjectOutputStream(new FileOutputStream(iliasRssItemFile));
        out.writeObject(saveThisObject);
        out.close();
    }

    class IliasRssDataSaverException extends RuntimeException {
        IliasRssDataSaverException(String message) {
            super("IliasRssDataSaverException - " + message);
        }
    }
}
