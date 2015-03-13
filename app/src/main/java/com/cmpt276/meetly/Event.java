package com.cmpt276.meetly;

import android.content.ContentValues;
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
     *       Note: values must contain 6 key-value pairs
     * @param values The values to create the event with
     */
    public Event(ContentValues values) {
        this.eventID = values.getAsLong(MySQLiteHelper.COLUMN_ID);
        this.title = values.getAsString(MySQLiteHelper.COLUMN_TITLE);
        this.date = EventsDataSource.stringToDate((values.getAsString(MySQLiteHelper.COLUMN_DATE)));
        this.location = values.getAsString(MySQLiteHelper.COLUMN_LOCATION);
        this.attendees = EventsDataSource.parseAttendees(values.getAsString(MySQLiteHelper.COLUMN_ATTENDEES));
        this.notes = values.getAsString(MySQLiteHelper.COLUMN_NOTES);
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
     * Prints out event details to logcat (24HR time)
     * @return
     */
    public void printEvent(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Log.i(TAG, "\nEvent ID: " + eventID
            + "\nEvent title: " + title
            + "\nEvent date: " + sdf.format(date)
            + "\nEvent location: " + location
            + "\nEvent Attendees: " + attendees.toString()
            + "\nEvent notes: " + notes);
    }

    /**
     * Prints out event details to logcat (12HR time)
     * @return
     */
    public void printEventS(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm a");

        Log.i(TAG, "\nEvent ID: " + eventID
                + "\nEvent title: " + title
                + "\nEvent date: " + sdf.format(date)
                + "\nEvent location: " + location
                + "\nEvent Attendees: " + attendees.toString()
                + "\nEvent notes: " + notes);
    }

    // Accessors
    public String getTitle() {return title;}

    public Date getDateAsDate() {return date;}

    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(this.date);
    }

    public String _12HRgetDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm a");
        return sdf.format(date);
    }

    public String getLocation() {return location;}

    public ArrayList<String> getAttendees() {return attendees;}

    public String getNotes(){ return notes;}

    public long getID() {return eventID;}

    //Basic Mutators
    public void setTitle(String title) { this.title = title;}

    public void setDate(Date date) { this.date = date;}

    public void setLocation(String location) { this.location = location;}

    public void setAttendees(ArrayList<String> attendees) {this.attendees = attendees;}

    public void setNotes(String notes){ this.notes = notes;}

    public void setID(long eventID) {this.eventID = eventID;}


}
