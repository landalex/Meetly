package com.cmpt276.meetly;

import android.content.ContentValues;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Holds information about an event, and provides functionality to get the event details in different formats
 */

public class Event {
    private long eventID;
    private int sharedEventID;
    private String title;
    private Calendar startDate;
    private Calendar endDate;
    private LatLng eventLocation;

    //used for all calendar/date formats in the app for events
    public final static SimpleDateFormat EVENT_DATEFORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CANADA);

    final private String TAG = "EventClass";



/// Event Constructors ///

    /**
     * Event Constructor using LatLng
     * @param title
     * @param startDate
     * @param endDate
     * @param location
     */
    public Event(long eventID, String title, Calendar startDate, Calendar endDate, LatLng location) {
        this.eventID = eventID;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        eventLocation = new LatLng(location.latitude,location.longitude);
        sharedEventID = -1;
    }

    /**
     * Event Copy Constructor
     * @param eventCopy The event to shallow copy
     */
    public Event(Event eventCopy) {
        this.eventID = eventCopy.getID();
        this.title = eventCopy.getTitle();
        this.startDate = eventCopy.getStartDate();
        this.endDate = eventCopy.getEndDate();
        eventLocation = eventCopy.eventLocation;
        this.sharedEventID = getSharedEventID();
    }

    /**
     * Event Constructor
     *       Note: values must contain 6 key-value pairs
     * @param values The values to create the event with
     */
    public Event(ContentValues values) {
        eventID = values.getAsLong(MySQLiteHelper.COLUMN_ID);
        title = values.getAsString(MySQLiteHelper.COLUMN_TITLE);
        String startDateAsString = (values.getAsString(MySQLiteHelper.COLUMN_STARTDATE));
        String endDateAsString = (values.getAsString(MySQLiteHelper.COLUMN_ENDDATE));

        SimpleDateFormat sdf = EVENT_DATEFORMAT;
        try{
            startDate.setTime(sdf.parse(startDateAsString));
            endDate.setTime(sdf.parse(endDateAsString));
        }catch (ParseException e){
            Log.e(TAG, "Failed to parse string into a Calendar object");
            e.printStackTrace();
        }

        eventLocation = new LatLng(
                values.getAsDouble(MySQLiteHelper.COLUMN_LATITUDE)
               ,values.getAsDouble(MySQLiteHelper.COLUMN_LONGITUDE)
        );
    }

    /**
     * Event Default Value Constructor
     * Gives properties default values
     * The event is unusable in this state until its properties have been assigned
     */
    public Event() {
        eventID = -1;
        title = null;
        startDate = null;
        endDate = null;
        eventLocation = null;
        sharedEventID = -1;
    }

    /**
     * Prints out event details to logcat (12HR time)
     * @return
     */
    public void printEvent(){
        SimpleDateFormat sdf = EVENT_DATEFORMAT;

        Log.i(TAG, "\nEvent ID: " + eventID
                + "\nEvent title: " + title
                + "\nEvent start date: " + sdf.format(startDate)
                + "\nEvent end date: " + sdf.format(endDate)
                + "\nEvent location: " + eventLocation.latitude + ", " + eventLocation.longitude
                + "\nEvent sharedEventId: " + sharedEventID);
    }

    // Accessors
    public String getTitle() {return title;}

    public Calendar getStartDate() {return startDate;}

    public Calendar getEndDate() {return endDate;}

    public LatLng getLocation() {return eventLocation;}

    public int getSharedEventID() { return sharedEventID; }

    public long getID() {return eventID;}

    //Basic Mutators
    public void setTitle(String title) { this.title = title;}

    public void setStartDate(Calendar startDate) {this.startDate = startDate;}

    public void setEndDate(Calendar endDate) {this.endDate = endDate;}

    public void setLocation(LatLng location) { eventLocation = location;}

    public void setSharedEventID(int sharedEventID) { this.sharedEventID = sharedEventID;}

    public void setID(long eventID) {this.eventID = eventID;}


}
