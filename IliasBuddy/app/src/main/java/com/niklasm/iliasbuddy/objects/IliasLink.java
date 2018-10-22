package com.niklasm.iliasbuddy.objects;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Links that the user can save for fast access to specific sites
 */
abstract class IliasLink {

    /**
     * The short description or name of the link
     */
    @NonNull
    final private String NAME;
    /**
     * The description of the link
     */
    @NonNull
    final private String DESCRIPTION;
    /**
     * The link (URL) itself
     */
    @NonNull
    final private String LINK;
    /**
     * The date of the creation of the link
     */
    @NonNull
    final private Date DATE_CREATION;

    /**
     * TODO: Think about having everything final, links should be editable... - make either a new entry or add setters
     *
     * @param NAME
     * @param DESCRIPTION
     * @param LINK
     * @param DATE_CREATION
     */
    IliasLink(@NonNull final String NAME, @NonNull final String DESCRIPTION,
              @NonNull final String LINK, @NonNull final Date DATE_CREATION) {
        this.NAME = NAME;
        this.DESCRIPTION = DESCRIPTION;
        this.LINK = LINK;
        this.DATE_CREATION = DATE_CREATION;
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
    Date getDATE_CREATION() {
        return DATE_CREATION;
    }

    abstract String getShareableLink();

    @NonNull
    public String getNAME() {
        return NAME;
    }
}
