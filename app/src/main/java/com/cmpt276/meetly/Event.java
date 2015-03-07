package com.cmpt276.meetly;

import android.content.Context;
import android.database.Cursor;
import android.nfc.Tag;
import android.util.Log;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
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

    final private String TAG = "EventClass";


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

    /**
     * Prints out event details to logcat
     * @return
     */
    public void printEvent(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Log.i(TAG, "\nEvent ID: " + eventID
            + "\nEvent title: " + title
            + "\nEvent date: " + dateFormat.format(date).toString()
            + "\nEvent location: " + location
            + "\nEvent Attendees: " + attendees.toString()
            + "\nEvent notes: " + notes);
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
