package com.niklasm.iliasbuddy.objects;

import android.support.annotation.NonNull;

import java.util.Date;

abstract class IliasReminder {
    @NonNull
    final private String NAME;
    @NonNull
    final private String DESCRIPTION;
    @NonNull
    final private String LINK;
    @NonNull
    final private Date DATE;

    /**
     * TODO: Think about having everything final, reminders should be editable... - make either a new entry or add setters
     *
     * @param DESCRIPTION
     * @param LINK
     * @param DATE
     */
    IliasReminder(@NonNull final String NAME, @NonNull final String DESCRIPTION,
                  @NonNull final String LINK, @NonNull final Date DATE) {
        this.NAME = NAME;
        this.DESCRIPTION = DESCRIPTION;
        this.LINK = LINK;
        this.DATE = DATE;
    }

    @NonNull
    String getDESCRIPTION() {
        return DESCRIPTION;
    }

    @NonNull
    String getLINK() {
        return LINK;
    }

    @NonNull
    Date getDATE() {
        return DATE;
    }

    @NonNull
    public String getNAME() {
        return NAME;
    }
}
