package com.cmpt276.meetly;

import java.util.Date;

/**
 * Created by AlexLand on 15-03-05.
 */
public class Event {
    private int _id;
    private String title;
    private String date;
    private String location;
    //attendees limit defaults to 25
    private String[] attendees = new String[25];
    private String notes;


    /**
     * Event Constructor
     * @param title
     * @param date
     * @param location
     * @param notes
     */
    public Event(String title, Date date, String location, String notes) {
        this.title = title;
        this.date = date.toString();
        this.location = location;
        this.notes = notes;
    }

    /**
     * Event Constructor
     * @param title
     * @param date
     * @param location
     * @param notes
     */
    public Event(String title, Date date, String location, String[] attendees, String notes) {
        this.title = title;
        this.date = date.toString();
        this.location = location;
        this.attendees = attendees;
        this.notes = notes;
    }

    /**
     * Event Copy Constructor
     * @param eventCopy : The event to copy
     */
    public Event(Event eventCopy) {
        this.title = eventCopy.getTitle();
        this.date = eventCopy.getTitle();
        this.location = eventCopy.getLocation();
        this.attendees = eventCopy.getAttendees();
        this.notes = eventCopy.getNotes();
    }

    /**
     * Event Constructor (created by Alex)
     * @param title
     * @param date
     */
    public Event(String title, Date date) {
        this.title = title;
        this.date = date.toString();
    }


    // Accessors
    public String getTitle() {return title;}

    public String getDate() {return date;}

    public String getLocation() {return location;}

    public String[] getAttendees() {return attendees;}

    public String getNotes(){ return notes;}

    //Basic Mutators
    public void setTitle(String newTitle) {title = newTitle;}

    public void setDate(Date newDate) {date = newDate.toString();}

    public void setLocation(String newLocation) {location = newLocation;}

    public void setAttendees(String[] newAttendees) {attendees = newAttendees;}

    public void setNotes(String newNotes){ notes = newNotes;}
}
