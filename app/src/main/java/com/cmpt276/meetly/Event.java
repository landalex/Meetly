package com.cmpt276.meetly;

import android.database.Cursor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by AlexLand on 15-03-05.
 */
public class Event {
    private long eventID;
    private String title;
    private Date date;
    private String location;
    private ArrayList<String> attendees;
    private String notes;


    /**
     * Event Constructor
     * @param title
     * @param date
     * @param location
     * @param notes
     */
    public Event(long eventID, String title, Date date, String location, ArrayList<String> attendees, String notes) {
        this.eventID = eventID;
        this.title = title;
        this.date = date;
        this.location = location;
        this.attendees = attendees;
        this.notes = notes;
    }

    /**
     * Event Copy Constructor
     * @param eventCopy The event to shallow copy
     */
    public Event(Event eventCopy) {
        this.eventID = eventCopy.getID();
        this.title = eventCopy.getTitle();
        this.date = eventCopy.getDateAsDate();
        this.location = eventCopy.getLocation();
        this.attendees = eventCopy.getAttendees();
        this.notes = eventCopy.getNotes();
    }

    /**
     * Event Constructor
     * Gives properties default values
     * The event is unusable in this state until its properties have been assigned
     */
    public Event() {
        this.eventID = -1;
        this.title = "";
        this.date = null;
        this.location = "";
        this.attendees = new ArrayList<String>();
        this.notes = "";
    }

    /**
     * Event Constructor (created by Alex)
     * @param title
     * @param date
     */
    public Event(String title, Date date) {
        this.title = title;
        this.date = date;
    }



    // Accessors
    public String getTitle() {return title;}

    public Date getDateAsDate() {return date;}

    public String getDate() {return date.toString();}

    public String getLocation() {return location;}

    public ArrayList<String> getAttendees() {return attendees;}

    public String getNotes(){ return notes;}

    public long getID() {return eventID;}

    //Basic Mutators
    public void setTitle(String title) { this.title = title;}

    public void setDate(Date date) { this.date = date;}

    public void setLocation(String location) { this.location = location;}

    public void setAttendees(ArrayList<String> attendees) {this.attendees = (ArrayList) attendees.clone();}

    public void setNotes(String notes){ this.notes = notes;}

    public void setID(long eventID) {this.eventID = eventID;}


}
