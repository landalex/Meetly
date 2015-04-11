package com.cmpt276.meetly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Provides a medium for interacting with the database
 */

public class EventsDataSource {


    final private String TAG = "EventsDataSource";

    private MySQLiteHelper dbHelper;
    private SQLiteDatabase database;
    private String[] dbColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_SHAREDEVENTID,
            MySQLiteHelper.COLUMN_TITLE,
            MySQLiteHelper.COLUMN_STARTDATE,
            MySQLiteHelper.COLUMN_ENDDATE,
            MySQLiteHelper.COLUMN_LATITUDE,
            MySQLiteHelper.COLUMN_LONGITUDE,
            MySQLiteHelper.COLUMN_VIEWED};
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
     * @param startDate start date for event
     * @param endDate end date for event
     * @param location LatLng object for event's location
     * @return a copy of the event added to the database
     */
    public Event createEvent(String title, Calendar startDate, Calendar endDate, LatLng location) {
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        //build record pairs
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, title);

        values.put(MySQLiteHelper.COLUMN_STARTDATE, Event.EVENT_DATEFORMAT.format(startDate.getTime()));
        values.put(MySQLiteHelper.COLUMN_ENDDATE, Event.EVENT_DATEFORMAT.format(endDate.getTime()));
        values.put(MySQLiteHelper.COLUMN_LATITUDE, location.latitude);
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, location.longitude);
        values.put(MySQLiteHelper.COLUMN_VIEWED, 1);
        //get row id and insert into database
        long insertID = database.insert(MySQLiteHelper.TABLE_EVENTS,null,values);


        //get this record and create new event object
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns,
                MySQLiteHelper.COLUMN_ID + " = " + insertID, null,null,null,null);
        resultSet.moveToFirst();
        Event event = new Event(insertID, title, startDate, endDate, location);
        resultSet.close();
        close();
        return event;
    }

    /**
     * Add a new shared event to the database
     * @param sharedEventID
     * @param title title for event
     * @param startDate start date for event
     * @param endDate end date for event
     * @param location LatLng object for event's location
     * @return false if event already in database
     */
    public boolean AddSharedEvent(long sharedEventID,String title, Calendar startDate, Calendar endDate, LatLng location) {
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        //build record pairs
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, title);

        values.put(MySQLiteHelper.COLUMN_STARTDATE, Event.EVENT_DATEFORMAT.format(startDate.getTime()));
        values.put(MySQLiteHelper.COLUMN_ENDDATE, Event.EVENT_DATEFORMAT.format(endDate.getTime()));
        values.put(MySQLiteHelper.COLUMN_LATITUDE, location.latitude);
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, location.longitude);
        values.put(MySQLiteHelper.COLUMN_VIEWED, 1);
        //get row id and insert into database
        long insertID = database.insert(MySQLiteHelper.TABLE_EVENTS,null,values);


        //get this record and create new event object
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns,
                MySQLiteHelper.COLUMN_ID + " = " + insertID, null,null,null,null);
        resultSet.moveToFirst();
        Event event = new Event(insertID, title, startDate, endDate, location);
        resultSet.close();
        close();
        return true;
    }


    /**
     * Create a new event from a ContentValues object and add to the database
     *      Note: Content values must have 6 key-value pairs
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
        Cursor resultSet = findEventCursorByID(eventID);
        return buildEventFromCursor(resultSet);
    }



    /**
     * TODO: Currently, this assumes all sharedEventID's are unique (they may not be unique for this table)
     * Retrieves an event matching the given ID
     * @param sharedEventID The event ID matching the database record
     * @return  The event from the database
     *          Returns null if no events in the database on this date
     * @throws java.sql.SQLException
     */
    public Event findEventBySharedID(long sharedEventID) throws RuntimeException{
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }

        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, MySQLiteHelper.COLUMN_SHAREDEVENTID + " = " + sharedEventID,null,null,null,null);

        if(resultSet.getCount() == 0){
            Log.d(TAG,"Error attempting to retrieve record from database. The ID \"\" + eventID + \"\" does not match any record in the database");
            return null;
        }else if(resultSet.getCount() > 1){
            throw new RuntimeException("Error attempting to retrieve record from database. The ID \"\" + eventID + \"\" matches more than 1 record in the database");
        }

        resultSet.moveToFirst();
        Log.i(TAG, "" + resultSet.getLong(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_ID)));
        return buildEventFromCursor(resultSet);
    }

    /**
     * Find all the events for a given date
     * @param startDate The date to retrieve events for
     * @return A list of events on this date
     *         Returns null if no events in the database on this date
     *
     */
    public List<Event> findEventsByStartDate(Calendar startDate) throws RuntimeException{
        database = dbHelper.getReadableDatabase();
        List<Event> events;
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, MySQLiteHelper.COLUMN_STARTDATE
                + " = " + Event.EVENT_DATEFORMAT.format(startDate.getTime()), null, null, null, "date");

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
        String locationQuery = MySQLiteHelper.COLUMN_LATITUDE + " = " + location.latitude + " AND " + MySQLiteHelper.COLUMN_LONGITUDE + " = " + location.latitude;
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
     * Adds a sharedEventID to the event specified by the given eventID
     * @param eventID The event to add the sharedEventID to
     * @param sharedEventID the ID to add to the event
     */
    public void addSharedEventID(int eventID, int sharedEventID){
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, MySQLiteHelper.COLUMN_SHAREDEVENTID + " = " + sharedEventID,null,null,null,null);
        Event event = buildEventFromCursor(resultSet);
        event.setSharedEventID(sharedEventID);
        updateDatabaseEvent(event);
    }

    /**
     * Commits an update to an event to the database
     * @param event The event details to commit to the database
     *                      Event must exist prior to calling this function
     */
    public void updateDatabaseEvent(Event event){
        try{
            open();
        }catch (SQLException e){
            Log.e(TAG, "Failed to open database for writing");
            e.printStackTrace();
        }

        //build record pairs
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_SHAREDEVENTID,event.getSharedEventID());
        values.put(MySQLiteHelper.COLUMN_TITLE, event.getTitle());
        values.put(MySQLiteHelper.COLUMN_STARTDATE, Event.EVENT_DATEFORMAT.format(event.getStartDate().getTime()));
        values.put(MySQLiteHelper.COLUMN_ENDDATE, Event.EVENT_DATEFORMAT.format(event.getEndDate().getTime()));
        values.put(MySQLiteHelper.COLUMN_LATITUDE, event.getLocation().latitude);
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, event.getLocation().longitude);


        int tempViewed = event.isViewed() ? 1 : 0;
        values.put(MySQLiteHelper.COLUMN_VIEWED, tempViewed);

        try{
            database.update(MySQLiteHelper.TABLE_EVENTS, values,MySQLiteHelper.COLUMN_ID + " = " + event.getID(),null);
        }catch (RuntimeException e){
            Log.e(TAG, "Event ID #" + event.getID() + " \"" + event.getTitle() + "\" failed to update");
        }

        Log.i(TAG, "Event ID #" + event.getID() + " \"" + event.getTitle() + "\" has been updated");
    }



    /**
     * Retrieves a cursor to an event matching the given ID in the database
     * @param eventID The event ID matching the database record
     * @return  The event from the database
     *          Returns null if no events in the database on this date
     * @throws java.sql.SQLException
     */
    private Cursor findEventCursorByID(long eventID) throws RuntimeException{
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
            //throw new RuntimeException("Error attempting to open the database");
        }

        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, MySQLiteHelper.COLUMN_ID + " = " + eventID,null,null,null,null);

        if(resultSet.getCount() == 0){
            throw new RuntimeException("Error attempting to retrieve record from database. The ID \"" + eventID + "\" does not match any record in the database");
        }

        resultSet.moveToFirst();
        Log.i(TAG, "" + resultSet.getLong(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_ID)));
        return resultSet;
    }



    /**
     * Builds a List containing events from a Cursor object
     * @param resultSet The cursor to build from
     * @return The list of events
     */
    private List<Event> buildEventsList(Cursor resultSet){
        List<Event> events = new ArrayList<>(0);

        try {
            resultSet.moveToFirst();

            do {
                Event event = buildEventFromCursor(resultSet);
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
    private Event buildEventFromCursor(Cursor resultSet){
        Event event = new Event();
        event.setID(resultSet.getLong(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_ID)));
        event.setSharedEventID(resultSet.getInt(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_SHAREDEVENTID)));
        event.setTitle(resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_TITLE)));

        //attempt to get dates from string and convert to Calendar
        String startDateAsString = resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_STARTDATE));
        String endDateAsString = resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_ENDDATE));
        Calendar calendar = new GregorianCalendar();
        try{
            calendar.setTime(Event.EVENT_DATEFORMAT.parse(startDateAsString));
            event.setStartDate(calendar);

            calendar.setTime(Event.EVENT_DATEFORMAT.parse(endDateAsString));
            event.setEndDate(calendar);
        }catch (ParseException e){
            e.printStackTrace();
        }

        double loc_lat = resultSet.getDouble(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_LATITUDE));
        double loc_long = resultSet.getDouble(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_LONGITUDE));
        LatLng location = new LatLng(loc_lat,loc_long);
        event.setLocation(location);

        event.setViewed(getBooleanFromCursor(resultSet, resultSet.getColumnIndex(MySQLiteHelper.COLUMN_VIEWED)));
        return event;
    }

    private boolean getBooleanFromCursor(Cursor resultSet, int columnIndex) {
        if (resultSet.isNull(columnIndex) || resultSet.getShort(columnIndex) == 0) {
            return false;
        } else {
            return true;
        }
    }

}
