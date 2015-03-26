package com.cmpt276.meetly;

import android.content.ContentValues;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Holds information about an event, and provides functionality to get the event details in different formats
 */

public class Event {
    private long eventID;
    private String title;
    private Date date;
    private LatLng eventLocation;
/*
    private ArrayList<String> attendees;
*/
    private int duration;
    private Calendar startTime;
    private Calendar endTime;

    final private String TAG = "EventClass";



/// Event Constructors ///

    /**
     * Event Constructor using LatLng
     * @param title
     * @param date
     * @param location
     * @param duration
     */
    public Event(long eventID, String title, Date date, LatLng location, int duration) {
        this.eventID = eventID;
        this.title = title;
        this.date = date;
        eventLocation = new LatLng(location.latitude,location.longitude);
        this.duration = duration;
    }

    /**
     * Event Copy Constructor
     * @param eventCopy The event to shallow copy
     */
    public Event(Event eventCopy) {
        this.eventID = eventCopy.getID();
        this.title = eventCopy.getTitle();
        this.date = eventCopy.getDate();
        eventLocation = eventCopy.eventLocation;
        this.duration = eventCopy.getDuration();
    }

    /**
     * Event Constructor
     *       Note: values must contain 6 key-value pairs
     * @param values The values to create the event with
     */
    public Event(ContentValues values) {
        eventID = values.getAsLong(MySQLiteHelper.COLUMN_ID);
        title = values.getAsString(MySQLiteHelper.COLUMN_TITLE);
        String dateAsString = (values.getAsString(MySQLiteHelper.COLUMN_DATE));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd hh:mm");

        try{
            date = sdf.parse(dateAsString);
        }catch (ParseException e){
            Log.e(TAG, "Failed to parse " + dateAsString + " into a date object");
            e.printStackTrace();
        }

        eventLocation = new LatLng(
                values.getAsDouble(MySQLiteHelper.COLUMN_LOCLAT)
               ,values.getAsDouble(MySQLiteHelper.COLUMN_LOCLAT)
        );

/*
        attendees = EventsDataSource.parseAttendees(values.getAsString(MySQLiteHelper.COLUMN_ATTENDEES));
*/
        duration = values.getAsInteger(MySQLiteHelper.COLUMN_DURATION);
    }

    /**
     * Event Default Value Constructor
     * Gives properties default values
     * The event is unusable in this state until its properties have been assigned
     */
    public Event() {
        this.eventID = -1;
        this.title = "";
        this.date = null;
        eventLocation = null;
        this.duration = -1;
    }

    /**
     * Prints out event details to logcat (12HR time)
     * @return
     */
    public void printEvent(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm a");

        Log.i(TAG, "\nEvent ID: " + eventID
                + "\nEvent title: " + title
                + "\nEvent date: " + sdf.format(date)
                + "\nEvent location: " + eventLocation.latitude + ", " + eventLocation.longitude
/*
                + "\nEvent Attendees: " + attendees.toString()
*/
                + "\nEvent duration: " + duration);
    }

    // Accessors
    public String getTitle() {return title;}

    public Date getDate() {return date;}

    public LatLng getLocation() {
        return eventLocation;
    }

/*
    public ArrayList<String> getAttendees() {return attendees;}
*/

    public int getDuration(){ return duration;}

    public long getID() {return eventID;}

    //Basic Mutators
    public void setTitle(String title) { this.title = title;}

    public void setDate(Date date) { this.date = date;}

    public void setLocation(LatLng location) { eventLocation = location;}

    /*public void setAttendees(ArrayList<String> attendees) {this.attendees = attendees;}*/

    public void setDuration(int duration){ this.duration = duration;}

    public void setID(long eventID) {this.eventID = eventID;}


}
