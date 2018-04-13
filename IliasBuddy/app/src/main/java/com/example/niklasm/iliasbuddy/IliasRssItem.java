package com.example.niklasm.iliasbuddy;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

public class IliasRssItem implements Comparator<IliasRssItem>, Serializable {
    private String course;
    private String title;
    private String link;
    private String description;

    public void setCourse(String course) {
        this.course = course;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private Date date;

    public IliasRssItem(final String course, final String title, final String link, final String description, final Date date) {
        this.course = course;
        this.title = title;
        this.link = link;
        this.description = description;
        this.date = date;
    }

    public String getCourse() {
        return course;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "course="+course+",title="+title+",link="+link+",description="+description+",date="+date.getTime();
    }

    public int compare(final IliasRssItem o1, final IliasRssItem o2) {
        final int dateIsTheSame = o1.getDate().compareTo(o2.getDate());
        if (dateIsTheSame != 0) return dateIsTheSame;
        // if date is the same check course
        final int courseIsTheSame = o1.getCourse().compareTo(o2.getCourse());
        if (courseIsTheSame != 0) return dateIsTheSame;
        // if course is the same check title
        final int titleIsTheSame = o1.getTitle().compareTo(o2.getTitle());
        if (titleIsTheSame != 0) return titleIsTheSame;
        // if title is the same check description
        final int descriptionIsTheSame = o1.getDescription().compareTo(o2.getDescription());
        if (descriptionIsTheSame != 0) return descriptionIsTheSame;
        // if description is the same check link
        return o1.getLink().compareTo(o2.getLink());
    }
}
