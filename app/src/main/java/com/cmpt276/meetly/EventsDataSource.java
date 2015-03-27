package com.cmpt276.meetly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Provides a medium for interacting with the database
 */

public class EventsDataSource {


    final private String TAG = "EventsDataSource";

    private MySQLiteHelper dbHelper;
    private SQLiteDatabase database;
    private String[] dbColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_TITLE,
            MySQLiteHelper.COLUMN_DATE,
            MySQLiteHelper.COLUMN_LOCLAT,
            MySQLiteHelper.COLUMN_LOCLONG,
            MySQLiteHelper.COLUMN_DURATION};
    /**
     * Events Data Source constructor.
     * Facilitates database connections and supports adding new events and fetching events
     * @param context Application context
     */
    public EventsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
 }

    /**
     * @throws SQLException
     */
    public void open() throws SQLException {database = dbHelper.getWritableDatabase();}

    public void close(){
        dbHelper.close();
    }

    /**
     * Create a new event and add to the database
     * @param title title for event
     * @param date date for event
     * @param location LatLng object for event's location
     * @return a copy of the event added to the database
     */
    public Event createEvent(String title, Date date, LatLng location, int duration) {
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        //build record pairs
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, title);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        values.put(MySQLiteHelper.COLUMN_DATE, sdf.format(date));
        values.put(MySQLiteHelper.COLUMN_LOCLAT, location.latitude);
        values.put(MySQLiteHelper.COLUMN_LOCLONG, location.longitude);
        values.put(MySQLiteHelper.COLUMN_DURATION, duration);

        //get row id and insert into database
        long insertID = database.insert(MySQLiteHelper.TABLE_EVENTS,null,values);


        //get this record and create new event object
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns,
                MySQLiteHelper.COLUMN_ID + " = " + insertID, null,null,null,null);
        resultSet.moveToFirst();
        Event event = new Event(insertID,title, date, location, duration);
        resultSet.close();
        close();
        return event;
    }


    /**
     * Create a new event from a ContentValues object and add to the database
     *      Note: Content values must have 5 key-value pairs
     * @param values The values to create the event with
     * @return a copy of the event added to the database
     */
    public Event createEvent(ContentValues values){
        try {
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        long insertID = database.insert(MySQLiteHelper.TABLE_EVENTS,null,values);
        Event event = new Event(values);
        close();
        return event;
    }

    /**
     * Deletes an event from the database
     * @param event The event to be deleted
     */
    public void deleteEvent(Event event){
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        long eventId = event.getID();
        database.delete(MySQLiteHelper.TABLE_EVENTS, MySQLiteHelper.COLUMN_ID + " = " + eventId, null);
        close();
        Log.i(TAG, "Event ID " + event.getID() + " has been deleted from the database.");
    }

    /**
     * Retrieves an event matching the given ID
     * @param eventID The event ID matching the database record
     * @return  The event from the database
     *          Returns null if no events in the database on this date
     * @throws java.sql.SQLException
     */
    public Event findEventByID(long eventID) throws RuntimeException{
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }

        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, MySQLiteHelper.COLUMN_ID + " = " + eventID,null,null,null,null);

        if(resultSet.getCount() == 0){
            throw new RuntimeException("Error attempting to retrieve record from database. The ID \"\" + eventID + \"\" does not match any record in the database");
        }

        resultSet.moveToFirst();
        Log.i(TAG, "" + resultSet.getLong(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_ID)));
        return getEventFromCursor(resultSet);
    }

    /**
     * Find all the events for a given date
     * @param date The date to retrieve events for
     * @return A list of events on this date
     *         Returns null if no events in the database on this date
     *
     */
    public List<Event> findEventsByDate(Date date) throws RuntimeException{
        database = dbHelper.getReadableDatabase();
        List<Event> events;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, MySQLiteHelper.COLUMN_DATE
                + " = " + sdf.format(date), null, null, null, "date");

        if(resultSet.getCount() == 0){
            throw new RuntimeException("Error attempting to retrieve record from database. The ID \"\" + eventID + \"\" does not match any record in the database");
        }
        events = buildEventsList(resultSet);
        return events;
    }

    /**
     * Retrieve all events in a given location
     * @param location The location to retrieve events from
     * @return  A list containing the events at this location
     *          Returns null if no events in the database on this date
     */
    public List<Event> findEventsByLocation(LatLng location) throws RuntimeException{
        database = dbHelper.getReadableDatabase();
        List<Event> events;
        String locationQuery = MySQLiteHelper.COLUMN_LOCLAT + " = " + location.latitude + " AND " + MySQLiteHelper.COLUMN_LOCLONG + " = " + location.latitude;
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, locationQuery,null,null,null,"date");

        if(resultSet.getCount() == 0){
            throw new RuntimeException("Error attempting to retrieve record from database. The ID \"\" + eventID + \"\" does not match any record in the database");
        }

        events = buildEventsList(resultSet);
        return events;
    }

    /**
     * Gets all the events in the database and returns them in a list of events
     * @return A list of events
     */
    public List<Event> getAllEvents(){
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        List<Event> events = new ArrayList<>(0);
        Cursor resultSet;
        Log.i(TAG, "Attempting to get all events in the database...");
        try{
            resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, null, null, null, null, null);
        }catch (Exception e){
            Log.e(TAG, "Failed to extract any events");
            e.printStackTrace();
            return events;
        }
        Log.i(TAG, "retrieved all events form the database with " + resultSet.getCount() + " records.");
        events = buildEventsList(resultSet);
        resultSet.close();
        close();
        Log.i(TAG, "finished getAllEvents...");
        return events;
    }

    /**
     * Commits an update to an event to the database
     * @param event The event details to commit to the database
     *                      Event must exist prior to calling this function
     */
    public void updateEvent(Event event){
        try{
            open();
        }catch (SQLException e){
            Log.e(TAG, "Failed to open database for writing");
            e.printStackTrace();
        }

        //build record pairs
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, event.getTitle());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        values.put(MySQLiteHelper.COLUMN_DATE, sdf.format(event.getDate()));
        values.put(MySQLiteHelper.COLUMN_LOCLAT, event.getLocation().latitude);
        values.put(MySQLiteHelper.COLUMN_LOCLONG, event.getLocation().longitude);
        values.put(MySQLiteHelper.COLUMN_DURATION, event.getDuration());

        try{
            database.update(MySQLiteHelper.TABLE_EVENTS, values,MySQLiteHelper.COLUMN_ID + " = " + event.getID(),null);
        }catch (RuntimeException e){
            Log.i(TAG, "Event ID #" + event.getID() + " \"" + event.getTitle() + "\" failed to update");
        }

        Log.i(TAG, "Event ID #" + event.getID() + " \"" + event.getTitle() + "\" has been updated");
    }

    /**
     * Builds a List containing events from a Cursor object
     * @param resultSet The cursor to build from
     * @return The list of events
     */
    private List<Event> buildEventsList(Cursor resultSet){
        List<Event> events = new ArrayList<Event>(0);

        try {
            resultSet.moveToFirst();

            //TODO: change to while?
            do {
                Event event = getEventFromCursor(resultSet);
                Log.i(TAG, "" + event.getID());
                events.add(event);
            } while (resultSet.moveToNext());

        }catch (CursorIndexOutOfBoundsException e){
            Log.e(TAG, "resultSet cursor returned no events!");
            e.printStackTrace();
            return events;
        }
        Log.i(TAG, "Finished building events list with size: " + events.size());
        return events;
    }


    /**
     * Gets an Event from a cursor to this database
     * @param resultSet cursor to the database
     * @return The Event from the database cursor points to
     */
    private Event getEventFromCursor(Cursor resultSet){
        Event event = new Event();
        event.setID(resultSet.getLong(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_ID)));
        event.setTitle(resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_TITLE)));

        //attempt to get date from string
        String dateAsString = resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_DATE));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/DD hh:mm");
        try{
            event.setDate(sdf.parse(dateAsString));
        }catch (ParseException e){
            e.printStackTrace();
        }

        double loc_lat = resultSet.getDouble(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_LOCLAT));
        double loc_long = resultSet.getDouble(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_LOCLONG));
        LatLng location = new LatLng(loc_lat,loc_long);
        event.setLocation(location);
        event.setDuration(resultSet.getInt(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_DURATION)));
        return event;
    }

}
