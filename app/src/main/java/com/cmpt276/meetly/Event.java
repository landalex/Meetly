package com.cmpt276.meetly;

import android.content.ContentValues;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;



public class Event {
    private long eventID;
    private String title;
    private Date date;
    private Double location_long;
    private Double location_lat;
    private ArrayList<String> attendees;
    private int duration;

    final private String TAG = "EventClass";


    /**
     * Event Constructor
     * @param title
     * @param date
     * @param loc_long
     * @param loc_lat
     * @param duration
     */
    public Event(long eventID, String title, Date date, double loc_long, double loc_lat, int duration) {
        this.eventID = eventID;
        this.title = title;
        this.date = date;
        this.location_lat = loc_lat;
        this.location_long = loc_long;
        this.duration = duration;
    }

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
        this.attendees = attendees;
    }

    /**
     * Event Copy Constructor
     * @param eventCopy The event to shallow copy
     */
    public Event(Event eventCopy) {
        this.eventID = eventCopy.getID();
        this.title = eventCopy.getTitle();
        this.date = eventCopy.getDateAsDate();
        ArrayList<Double> location = eventCopy.getLocation();
        this.location_lat = location.get(0);
        this.location_long = location.get(1);
        this.duration = eventCopy.getDuration();
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

        this.location_long = values.getAsDouble(MySQLiteHelper.COLUMN_LOCLONG);
        this.location_lat = values.getAsDouble(MySQLiteHelper.COLUMN_LOCLAT);


        this.attendees = EventsDataSource.parseAttendees(values.getAsString(MySQLiteHelper.COLUMN_ATTENDEES));
        this.duration = values.getAsInteger(MySQLiteHelper.COLUMN_DURATION);
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
        this.location_lat = -1.0;
        this.location_long = -1.0;
        this.duration = -1;
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
            + "\nEvent location: " + location_lat + ", " + location_long
            + "\nEvent Attendees: " + attendees.toString()
            + "\nEvent duration: " + duration);
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
                + "\nEvent location: " + location_lat + ", " + location_long
                + "\nEvent Attendees: " + attendees.toString()
                + "\nEvent duration: " + duration);
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

    public ArrayList<Double> getLocation() {
        return new ArrayList<Double>(Arrays.asList(location_long,location_lat));
    }

    public ArrayList<String> getAttendees() {return attendees;}

    public int getDuration(){ return duration;}

    public long getID() {return eventID;}

    //Basic Mutators
    public void setTitle(String title) { this.title = title;}

    public void setDate(Date date) { this.date = date;}

    public void setLocation(double loc_lat, double loc_long) {
        this.location_lat = loc_lat;
        this.location_long = loc_long;
    }

    public void setAttendees(ArrayList<String> attendees) {this.attendees = attendees;}

    public void setDuration(int duration){ this.duration = duration;}

    public void setID(long eventID) {this.eventID = eventID;}


}
