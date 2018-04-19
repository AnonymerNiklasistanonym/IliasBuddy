package com.example.niklasm.iliasbuddy.IliasRssClasses;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

public class IliasRssItem implements Comparator<IliasRssItem>, Serializable {
    final private String course;
    final private String extra;
    final private String titleExtra;
    final private String title;
    final private String description;
    final private String link;
    final private Date date;

    IliasRssItem(final String course, final String extra, final String titleExtra, final String title, final String description, final String link, final Date date) {
        this.course = course;
        this.extra = extra;
        this.titleExtra = titleExtra;
        this.title = title;
        this.description = description;
        this.link = link;
        this.date = date;
    }

    public String getCourse() {
        return course;
    }

    public String getExtra() {
        return extra;
    }

    public String getTitleExtra() {
        return titleExtra;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "course=" + course + ",extra=" + extra + ",titleExtra=" + titleExtra + ",title=" + title + ",description=" + description + ",link=" + link + ",date=" + date.getTime();
    }

    public int compare(final IliasRssItem o1, final IliasRssItem o2) {
        final int dateIsTheSame = o1.getDate().compareTo(o2.getDate());
        if (dateIsTheSame != 0) return dateIsTheSame;
        // if date is the same check course
        final int courseIsTheSame = o1.getCourse().compareTo(o2.getCourse());
        if (courseIsTheSame != 0) return dateIsTheSame;
        // if course is the same check extra
        final int extraIsTheSame = o1.getExtra().compareTo(o2.getExtra());
        if (extraIsTheSame != 0) return extraIsTheSame;
        // if extra is the same check titleExtra
        final int titleExtraIsTheSame = o1.getTitleExtra().compareTo(o2.getTitleExtra());
        if (titleExtraIsTheSame != 0) return titleExtraIsTheSame;
        // if titleExtra is the same check title
        final int titleIsTheSame = o1.getTitle().compareTo(o2.getTitle());
        if (titleIsTheSame != 0) return titleIsTheSame;
        // if title is the same check description
        final int descriptionIsTheSame = o1.getDescription().compareTo(o2.getDescription());
        if (descriptionIsTheSame != 0) return descriptionIsTheSame;
        // if description is the same check link
        return o1.getLink().compareTo(o2.getLink());
    }

}
