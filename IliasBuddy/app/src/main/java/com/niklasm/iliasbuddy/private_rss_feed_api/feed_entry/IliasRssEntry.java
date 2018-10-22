package com.niklasm.iliasbuddy.private_rss_feed_api.feed_entry;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Html;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class IliasRssEntry implements IIliasRssEntry {
    public static final Creator<IliasRssEntry> CREATOR = new Creator<IliasRssEntry>() {
        @NonNull
        @Override
        public IliasRssEntry createFromParcel(final Parcel in) {
            return new IliasRssEntry(in);
        }

        @Override
        public IliasRssEntry[] newArray(final int size) {
            return new IliasRssEntry[size];
        }
    };
    @NonNull
    final public String COURSE;
    final public String TITLE_EXTRA;
    @NonNull
    final public String TITLE;
    @NonNull
    final public String DESCRIPTION;
    @NonNull
    final public String LINK;
    @NonNull
    final public Date DATE;
    final public String EXTRA;

    /**
     * Constructor that creates a new IliasEntry
     *
     * @param COURSE      Name of the Ilias course of the RSS entry ("Math for Informatics")
     * @param TITLE       Title of the course/file update ("Question to A4"/"File was updated.")
     * @param TITLE_EXTRA Extra information about where the entry is ("Forum") which can be null
     * @param DESCRIPTION Content/Description of the Ilias course of the RSS entry
     * @param LINK        Link to Ilias post of the RSS entry
     * @param DATE        Date of the RSS entry
     * @param EXTRA       Is not null if there was a file update ("FileName.pdf")
     */
    public IliasRssEntry(@NonNull final String COURSE, final String TITLE_EXTRA,
                         @NonNull final String TITLE, @NonNull final String DESCRIPTION,
                         @NonNull final String LINK, @NonNull final Date DATE, final String EXTRA) {
        this.COURSE = COURSE;
        this.TITLE_EXTRA = TITLE_EXTRA;
        this.TITLE = TITLE;
        this.DESCRIPTION = DESCRIPTION;
        this.LINK = LINK;
        this.DATE = DATE;
        this.EXTRA = EXTRA;
    }

    public IliasRssEntry(@NonNull final IliasRssEntry iliasRssEntry) {
        COURSE = iliasRssEntry.COURSE;
        TITLE_EXTRA = iliasRssEntry.TITLE_EXTRA;
        TITLE = iliasRssEntry.TITLE;
        DESCRIPTION = iliasRssEntry.DESCRIPTION;
        LINK = iliasRssEntry.LINK;
        DATE = iliasRssEntry.DATE;
        EXTRA = iliasRssEntry.EXTRA;
    }

    private IliasRssEntry(@NonNull final Parcel IN) {
        COURSE = IN.readString();
        TITLE_EXTRA = IN.readString();
        TITLE = IN.readString();
        DESCRIPTION = IN.readString();
        LINK = IN.readString();
        DATE = new Date(IN.readLong());
        EXTRA = IN.readString();
    }

    public static IliasRssEntry[] readParcelableArray(final Parcelable[] parcelables) {
        if (parcelables == null) {
            return null;
        }
        return Arrays.copyOf(parcelables, parcelables.length, IliasRssEntry[].class);
    }

    /**
     * @return IliasRssFeedItem entry is a file update
     */
    public boolean isFileUpdate() {
        return DESCRIPTION.equals("");
    }

    @Override
    public String toString() {
        return "course=" + COURSE + ",titleExtra=" + TITLE_EXTRA + ",title=" +
                TITLE + ",description=" + DESCRIPTION + ",link=" + LINK +
                ",date=" + DATE.getTime() + ",extra=" + EXTRA;
    }

    /**
     * @param CONTEXT Get Context for locale
     * @return Notification preview text for single notification
     */
    @NonNull
    public String toStringNotificationPreview(@NonNull final Context CONTEXT) {
        return ">> " + (TITLE_EXTRA != null ? TITLE_EXTRA + ": " : "") + TITLE + "\n(" +
                new SimpleDateFormat(IIliasRssEntry.SIMPLE_TIME_DATE_FORMAT,
                        CONTEXT.getResources().getConfiguration().locale).format(DATE) + ")";
    }

    /**
     * @param CONTEXT Get Context for locale
     * @return Notification text for multiple notification big
     */
    @NonNull
    public String toStringNotificationBigMultiple(@NonNull final Context CONTEXT) {
        return COURSE + (EXTRA != null ? " > " + EXTRA : "") +
                toStringNotificationPreview(CONTEXT);
    }

    /**
     * @param CONTEXT Get Context for locale
     * @return Notification preview text for one notification big
     */
    public String toStringNotificationBigSingle(@NonNull final Context CONTEXT) {
        return toStringNotificationPreview(CONTEXT) +
                (!DESCRIPTION.equals("") ? "\n\n" + Html.fromHtml(DESCRIPTION) : "");
    }

    @Override
    public int compare(final IliasRssEntry OBJECT_1, final IliasRssEntry OBJECT_2) {
        final int DATE_IS_THE_SAME = OBJECT_1.DATE.compareTo(OBJECT_2.DATE);
        if (DATE_IS_THE_SAME != 0) {
            return DATE_IS_THE_SAME;
        }
        final int COURSE_IS_THE_SAME = OBJECT_1.COURSE.compareTo(OBJECT_2.COURSE);
        if (COURSE_IS_THE_SAME != 0) {
            return DATE_IS_THE_SAME;
        }
        final int TITLE_EXTRA_IS_THE_SAME =
                OBJECT_1.TITLE_EXTRA.compareTo(OBJECT_2.TITLE_EXTRA);
        if (TITLE_EXTRA_IS_THE_SAME != 0) {
            return TITLE_EXTRA_IS_THE_SAME;
        }
        final int TITLE_IS_THE_SAME = OBJECT_1.TITLE.compareTo(OBJECT_2.TITLE);
        if (TITLE_IS_THE_SAME != 0) {
            return TITLE_IS_THE_SAME;
        }
        final int DESCRIPTION_IS_THE_SAME =
                OBJECT_1.DESCRIPTION.compareTo(OBJECT_2.DESCRIPTION);
        if (DESCRIPTION_IS_THE_SAME != 0) {
            return DESCRIPTION_IS_THE_SAME;
        }
        final int EXTRA_IS_THE_SAME = OBJECT_1.EXTRA.compareTo(OBJECT_2.EXTRA);
        if (EXTRA_IS_THE_SAME != 0) {
            return EXTRA_IS_THE_SAME;
        }
        return OBJECT_1.LINK.compareTo(OBJECT_2.LINK);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel OUT, final int i) {
        OUT.writeString(COURSE);
        OUT.writeString(TITLE_EXTRA);
        OUT.writeString(TITLE);
        OUT.writeString(DESCRIPTION);
        OUT.writeString(LINK);
        OUT.writeLong(DATE.getTime());
        OUT.writeString(EXTRA);
    }

    /**
     * Check if a search query is contained in any property of this object
     * without considering lower/upper case on both sides
     *
     * @param QUERY       Search query
     * @param DATE_FORMAT Time format of recycler view for the date
     * @param TIME_FORMAT Time format of recycler view for the time
     * @return Query is contained in at least one of the properties
     */
    public boolean containsIgnoreCase(@NonNull final String QUERY,
                                      @NonNull final SimpleDateFormat DATE_FORMAT,
                                      @NonNull final SimpleDateFormat TIME_FORMAT) {

        // check if search string is safe to work with
        if (QUERY.isEmpty()) {
            return false;
        }

        // convert search string to lowercase
        final String LOWER_CASE_QUERY = QUERY.toLowerCase();

        // check if in any property the search string is contained (ignoring upper-/lowercase)
        return COURSE.toLowerCase().contains(LOWER_CASE_QUERY)
                || (EXTRA != null && EXTRA.toLowerCase().contains(LOWER_CASE_QUERY))
                || (TITLE_EXTRA != null && TITLE_EXTRA.toLowerCase().contains(LOWER_CASE_QUERY))
                || TITLE.toLowerCase().contains(LOWER_CASE_QUERY)
                || DESCRIPTION.toLowerCase().contains(LOWER_CASE_QUERY)
                || LINK.toLowerCase().contains(LOWER_CASE_QUERY)
                || DATE_FORMAT.format(DATE).contains(LOWER_CASE_QUERY)
                || TIME_FORMAT.format(DATE).contains(LOWER_CASE_QUERY);
    }
}
