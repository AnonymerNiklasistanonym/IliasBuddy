package com.example.niklasm.iliasbuddy.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class IliasRssFeedItem implements Comparator<IliasRssFeedItem>, Serializable, Parcelable {

    public static final Creator<IliasRssFeedItem> CREATOR = new Creator<IliasRssFeedItem>() {
        @NonNull
        @Override
        public IliasRssFeedItem createFromParcel(final Parcel in) {
            return new IliasRssFeedItem(in);
        }

        @Override
        public IliasRssFeedItem[] newArray(final int size) {
            return new IliasRssFeedItem[size];
        }
    };

    @NonNull
    final private String COURSE;
    final private String TITLE_EXTRA;
    @NonNull
    final private String TITLE;
    @NonNull
    final private String DESCRIPTION;
    @NonNull
    final private String LINK;
    @NonNull
    final private Date DATE;
    final private String EXTRA;

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
    public IliasRssFeedItem(@NonNull final String COURSE, final String TITLE_EXTRA,
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

    private IliasRssFeedItem(@NonNull final Parcel IN) {
        COURSE = IN.readString();
        TITLE_EXTRA = IN.readString();
        TITLE = IN.readString();
        DESCRIPTION = IN.readString();
        LINK = IN.readString();
        DATE = new Date(IN.readLong());
        EXTRA = IN.readString();
    }

    /**
     * @return Ilias course name of the RSS entry ("Math for Informatics")
     */
    @NonNull
    public String getCourse() {
        return COURSE;
    }

    /**
     * @return Ilias location of entry ("Forum")
     */
    public String getTitleExtra() {
        return TITLE_EXTRA;
    }

    /**
     * @return Title of the Ilias post/file update ("Question exercise 4"/"File was updated.")
     */
    @NonNull
    public String getTitle() {
        return TITLE;
    }

    /**
     * @return Description of the Ilias post or in case of a file an empty String
     * ("My question is...plz help."/"")
     */
    @NonNull
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * @return HTTPS URL link to Ilias entry  of the RSS entry
     */
    @NonNull
    public String getLink() {
        return LINK;
    }

    @NonNull
    public Date getDate() {
        return DATE;
    }

    public String getExtra() {
        return EXTRA;
    }

    /**
     * @return IliasRssFeedItem entry is a file update
     */
    public boolean isFileUpdate() {
        return DESCRIPTION.equals("");
    }


    @Override
    public String toString() {
        return "course=" + COURSE + ",titleExtra=" + TITLE_EXTRA + ",title=" + TITLE +
                ",description=" + DESCRIPTION + ",link=" + LINK + ",date=" + DATE.getTime() +
                ",extra=" + EXTRA;
    }

    /**
     * @param VIEW_FORMAT_DATE Readable time format of notification
     * @return Notification preview text for single notification
     */
    @NonNull
    public String toStringNotificationSingle(@NonNull final SimpleDateFormat VIEW_FORMAT_DATE) {
        return ">> " + (TITLE_EXTRA != null ? TITLE_EXTRA + ": " : "")
                + TITLE + "\n(" + VIEW_FORMAT_DATE.format(DATE) + ")";
    }

    /**
     * @param VIEW_FORMAT_DATE Readable time format of notification
     * @return Notification preview text for multiple notification
     */
    @NonNull
    public String toStringNotificationMultiple(@NonNull final SimpleDateFormat VIEW_FORMAT_DATE) {
        return COURSE + (EXTRA != null ? " > " + EXTRA : "") +
                toStringNotificationSingle(VIEW_FORMAT_DATE);
    }

    @Override
    public int compare(final IliasRssFeedItem OBJECT_1, final IliasRssFeedItem OBJECT_2) {
        final int DATE_IS_THE_SAME = OBJECT_1.getDate().compareTo(OBJECT_2.getDate());
        if (DATE_IS_THE_SAME != 0) {
            return DATE_IS_THE_SAME;
        }
        final int COURSE_IS_THE_SAME = OBJECT_1.getCourse().compareTo(OBJECT_2.getCourse());
        if (COURSE_IS_THE_SAME != 0) {
            return DATE_IS_THE_SAME;
        }
        final int TITLE_EXTRA_IS_THE_SAME =
                OBJECT_1.getTitleExtra().compareTo(OBJECT_2.getTitleExtra());
        if (TITLE_EXTRA_IS_THE_SAME != 0) {
            return TITLE_EXTRA_IS_THE_SAME;
        }
        final int TITLE_IS_THE_SAME = OBJECT_1.getTitle().compareTo(OBJECT_2.getTitle());
        if (TITLE_IS_THE_SAME != 0) {
            return TITLE_IS_THE_SAME;
        }
        final int DESCRIPTION_IS_THE_SAME =
                OBJECT_1.getDescription().compareTo(OBJECT_2.getDescription());
        if (DESCRIPTION_IS_THE_SAME != 0) {
            return DESCRIPTION_IS_THE_SAME;
        }
        final int EXTRA_IS_THE_SAME = OBJECT_1.getExtra().compareTo(OBJECT_2.getExtra());
        if (EXTRA_IS_THE_SAME != 0) {
            return EXTRA_IS_THE_SAME;
        }
        return OBJECT_1.getLink().compareTo(OBJECT_2.getLink());
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
