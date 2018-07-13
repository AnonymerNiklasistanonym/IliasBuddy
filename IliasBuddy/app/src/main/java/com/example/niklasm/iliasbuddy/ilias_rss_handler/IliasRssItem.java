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
    final private String COURSE;
    final private String EXTRA;
    final private String TITLE_EXTRA;
    final private String TITLE;
    final private String DESCRIPTION;
    final private String LINK;
    final private Date DATE;

    IliasRssItem(@NonNull final String COURSE, final String EXTRA, final String TITLE_EXTRA,
                 @NonNull final String TITLE, final String DESCRIPTION, @NonNull final String LINK,
                 @NonNull final Date DATE) {
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

    public String toStringNotificationPreview(final SimpleDateFormat viewDateFormat) {
        return getCourse() + (getExtra() != null ? " > " + getExtra() : "") + " >> " +
                (getTitleExtra() != null ? getTitleExtra() + ": " : "") + getTitle() + " (" +
                viewDateFormat.format(getDate()) + ")";
    }

    public String toStringNotificationPreview2(final SimpleDateFormat viewDateFormat) {
        return ">> " + (getTitleExtra() != null ? getTitleExtra() + ": " : "") + getTitle() + "\n(" +
                viewDateFormat.format(getDate()) + ")";
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
        final int TITLE_EXTRA_IS_THE_SAME = OBJECT_1.getTitleExtra().compareTo(OBJECT_2.getTitleExtra());
        if (TITLE_EXTRA_IS_THE_SAME != 0) {
            return TITLE_EXTRA_IS_THE_SAME;
        }
        // if titleExtra is the same check title
        final int TITLE_IS_THE_SAME = OBJECT_1.getTitle().compareTo(OBJECT_2.getTitle());
        if (TITLE_IS_THE_SAME != 0) {
            return TITLE_IS_THE_SAME;
        }
        // if title is the same check description
        final int DESCRIPTION_IS_THE_SAME = OBJECT_1.getDescription().compareTo(OBJECT_2.getDescription());
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
}
