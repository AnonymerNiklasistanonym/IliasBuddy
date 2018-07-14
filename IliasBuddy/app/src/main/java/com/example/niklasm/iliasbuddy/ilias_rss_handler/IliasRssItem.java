package com.example.niklasm.iliasbuddy.ilias_rss_handler;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class IliasRssItem implements Comparator<IliasRssItem>, Serializable, Parcelable {

    public static final Creator<IliasRssItem> CREATOR = new Creator<IliasRssItem>() {
        @Override
        public IliasRssItem createFromParcel(final Parcel in) {
            return new IliasRssItem(in);
        }

        @Override
        public IliasRssItem[] newArray(final int size) {
            return new IliasRssItem[size];
        }
    };
    /**
     * Name of Ilias course: "Math for Informatics"
     */
    @NonNull
    final private String COURSE;
    /**
     * CAN BE NULL: "Forum"
     */
    final private String EXTRA;
    /**
     * CAN BE NULL: "New file"
     */
    final private String TITLE_EXTRA;
    /**
     * Title of entry: "I need help with exercise 6.2"
     */
    @NonNull
    final private String TITLE;
    /**
     * Description/text of Ilias entry: "Hello all, I have a question because..."
     */
    @NonNull
    final private String DESCRIPTION;
    /**
     * Link to Ilias entry
     */
    @NonNull
    final private String LINK;
    /**
     * Date of entry creation
     */
    @NonNull
    final private Date DATE;

    /**
     * Constructor that creates a new IliasEntry
     *
     * @param COURSE      (String) - Name of the Ilias course of the RSS entry
     * @param EXTRA       (String) - TODO
     * @param TITLE_EXTRA (String) - TODO
     * @param TITLE       (String) - Title of the Ilias course of the RSS entry
     * @param DESCRIPTION (String) - Content/Description of the Ilias course of the RSS entry
     * @param LINK        (String) - Link to Ilias post of the RSS entry
     * @param DATE        (String) - Date of the RSS entry
     */
    IliasRssItem(@NonNull final String COURSE, final String EXTRA, final String TITLE_EXTRA,
                 @NonNull final String TITLE, @NonNull final String DESCRIPTION,
                 @NonNull final String LINK, @NonNull final Date DATE) {
        this.COURSE = COURSE;
        this.EXTRA = EXTRA;
        this.TITLE_EXTRA = TITLE_EXTRA;
        this.TITLE = TITLE;
        this.DESCRIPTION = DESCRIPTION;
        this.LINK = LINK;
        this.DATE = DATE;
    }

    private IliasRssItem(@NonNull final Parcel IN) {
        COURSE = IN.readString();
        EXTRA = IN.readString();
        TITLE_EXTRA = IN.readString();
        TITLE = IN.readString();
        DESCRIPTION = IN.readString();
        LINK = IN.readString();
        DATE = new Date(IN.readLong());
    }

    public String getCourse() {
        return COURSE;
    }

    public String getExtra() {
        return EXTRA;
    }

    public String getTitleExtra() {
        return TITLE_EXTRA;
    }

    public String getTitle() {
        return TITLE;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public String getLink() {
        return LINK;
    }

    public Date getDate() {
        return DATE;
    }

    @Override
    public String toString() {
        return "course=" + COURSE + ",extra=" + EXTRA + ",titleExtra=" + TITLE_EXTRA +
                ",title=" + TITLE + ",description=" + DESCRIPTION + ",link=" + LINK +
                ",date=" + DATE.getTime();
    }

    /**
     * TODO - Make it external or rename this method
     */
    public String toStringNotificationPreview(@NonNull final SimpleDateFormat VIEW_FORMAT_DATE) {
        return COURSE + (EXTRA != null ? " > " + EXTRA : "") + " >> " +
                (TITLE_EXTRA != null ? TITLE_EXTRA + ": " : "") + TITLE +
                " (" + VIEW_FORMAT_DATE.format(DATE) + ")";
    }

    /**
     * TODO - Make it external or rename this method
     */
    public String toStringNotificationPreview2(@NonNull final SimpleDateFormat VIEW_FORMAT_DATE) {
        return ">> " + (TITLE_EXTRA != null ? TITLE_EXTRA + ": " : "") +
                TITLE + "\n(" + VIEW_FORMAT_DATE.format(DATE) + ")";
    }

    @Override
    public int compare(final IliasRssItem OBJECT_1, final IliasRssItem OBJECT_2) {
        final int DATE_IS_THE_SAME = OBJECT_1.getDate().compareTo(OBJECT_2.getDate());
        if (DATE_IS_THE_SAME != 0) {
            return DATE_IS_THE_SAME;
        }
        // if date is the same check course
        final int COURSE_IS_THE_SAME = OBJECT_1.getCourse().compareTo(OBJECT_2.getCourse());
        if (COURSE_IS_THE_SAME != 0) {
            return DATE_IS_THE_SAME;
        }
        // if course is the same check extra
        final int EXTRA_IS_THE_SAME = OBJECT_1.getExtra().compareTo(OBJECT_2.getExtra());
        if (EXTRA_IS_THE_SAME != 0) {
            return EXTRA_IS_THE_SAME;
        }
        // if extra is the same check titleExtra
        final int TITLE_EXTRA_IS_THE_SAME =
                OBJECT_1.getTitleExtra().compareTo(OBJECT_2.getTitleExtra());
        if (TITLE_EXTRA_IS_THE_SAME != 0) {
            return TITLE_EXTRA_IS_THE_SAME;
        }
        // if titleExtra is the same check title
        final int TITLE_IS_THE_SAME = OBJECT_1.getTitle().compareTo(OBJECT_2.getTitle());
        if (TITLE_IS_THE_SAME != 0) {
            return TITLE_IS_THE_SAME;
        }
        // if title is the same check description
        final int DESCRIPTION_IS_THE_SAME =
                OBJECT_1.getDescription().compareTo(OBJECT_2.getDescription());
        if (DESCRIPTION_IS_THE_SAME != 0) {
            return DESCRIPTION_IS_THE_SAME;
        }
        // if description is the same check link
        return OBJECT_1.getLink().compareTo(OBJECT_2.getLink());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel OUT, final int i) {
        OUT.writeString(COURSE);
        OUT.writeString(EXTRA);
        OUT.writeString(TITLE_EXTRA);
        OUT.writeString(TITLE);
        OUT.writeString(DESCRIPTION);
        OUT.writeString(LINK);
        OUT.writeLong(DATE.getTime());
    }

    /**
     * Check if a search query is contained in any property of this object
     * without considering lower/upper case on both sides
     *
     * @param QUERY       (String) - Search query
     * @param DATE_FORMAT (SimpleDateFormat) - Time format of recycler view for the date
     * @param TIME_FORMAT (SimpleDateFormat) - Time format of recycler view for the time
     * @return (boolean) Query is contained in at least one of the properties
     */
    public boolean containsIgnoreCase(@NonNull final String QUERY,
                                      @NonNull final SimpleDateFormat DATE_FORMAT,
                                      @NonNull final SimpleDateFormat TIME_FORMAT) {
        // check if search string is safe to work with
        if (QUERY.isEmpty()) {
            return false;
        } else {
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
}
