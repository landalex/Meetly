package com.cmpt276.meetly;

import java.util.Date;

/**
 * Created by AlexLand on 15-03-05.
 */
public class Event {
    private String title;
    private String date;

    public Event(String title, Date date) {
        this.title = title;
        this.date = date.toString();
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }
}
