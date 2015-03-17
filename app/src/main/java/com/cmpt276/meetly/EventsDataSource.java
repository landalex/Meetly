package com.cmpt276.meetly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.util.Log;

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

    //Testing properties
    private ArrayList<String> testArray = new ArrayList<String>();
    Event testEvent;





    /**
     * Events Data Source constructor.
     * Facilitates database connections and supports adding new events and fetching events
     * @param context Application context
     */
    public EventsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
        // performTests();
        // insertData();
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
     * @param loc_lat location  for event
     * @param loc_long notes  for event
     * @return a copy of the event added to the database
     */
    public Event createEvent(String title, Date date, double loc_lat, double loc_long, int duration) {
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
        values.put(MySQLiteHelper.COLUMN_LOCLAT, loc_lat);
        values.put(MySQLiteHelper.COLUMN_LOCLONG, loc_long);
        values.put(MySQLiteHelper.COLUMN_DURATION, duration);

        //get row id and insert into database
        long insertID = database.insert(MySQLiteHelper.TABLE_EVENTS,null,values);


        //get this record and create new event object
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns,
                MySQLiteHelper.COLUMN_ID + " = " + insertID, null,null,null,null);
        resultSet.moveToFirst();
        Event event = new Event(insertID,title, date, loc_lat, loc_long, duration);
        resultSet.close();
        close();
        return event;
    }

    /**
     * Create a new event and add to the database
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

        //get this record and create new event object
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns,
                MySQLiteHelper.COLUMN_ID + " = " + insertID, null,null,null,null);
        resultSet.moveToFirst();
        Event event = new Event(values);
        resultSet.close();
        close();
        return event;
    }


    /**
     * Parses a string of attendees for an event into substrings, and loads them into an ArrayList
     * @param attendeeString the ArrayList of attendees
     * @return the ArrayList of attendees
     */
    public static ArrayList<String> parseAttendees(String attendeeString){
        String subString = attendeeString.substring(1,attendeeString.length()-1);
        return new ArrayList<String>(Arrays.asList(subString.split(",")));
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
     */
    public Event findEventByID(long eventID){
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }

        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, MySQLiteHelper.COLUMN_ID + " = " + eventID,null,null,null,null);

        if(resultSet.getCount() == 0){
            Log.e(TAG, "Error attempting to retrieve record from database. The ID \"" + eventID + "\" does not match any record in the database");
            return null;
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
    public List<Event> findEventsByDate(Date date){
        database = dbHelper.getReadableDatabase();
        List<Event> events;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, MySQLiteHelper.COLUMN_DATE
                + " = " + sdf.format(date), null, null, null, "date");
        if(resultSet.getCount() == 0){
            Log.e(TAG, "Error attempting to retrieve records from database. There are not events on this date");
            return null;
        }
        events = buildEventsList(resultSet);
        return events;
    }

    /**
     * TODO: switch location to 2 doubles and match with MYSQLITEHelper
     * Retrieve all events in a given location
     * @param location The location to retrieve events from
     * @return  A list containing the events at this location
     *          Returns null if no events in the database on this date
     *
    public List<Event> findEventsByLocation(String location){
        database = dbHelper.getReadableDatabase();
        List<Event> events;
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns,MySQLiteHelper.COLUMN_
                + " = " + location,null,null,null,"date");

        if(resultSet.getCount() == 0){
            Log.e(TAG, "Error attempting to retrieve records from database. There are no events in this location");
            return null;
        }

        events = buildEventsList(resultSet);
        return events;
    }*/

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
        Log.i(TAG, "retrieved all events form the database with " + resultSet.getCount() + " records. Building l=events list");

        //if(events.size())
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
        values.put(MySQLiteHelper.COLUMN_DATE, event.getDate());
        ArrayList<Double> location = event.getLocation();
        values.put(MySQLiteHelper.COLUMN_LOCLAT, location.get(0));
        values.put(MySQLiteHelper.COLUMN_LOCLONG, location.get(1));
        values.put(MySQLiteHelper.COLUMN_ATTENDEES,event.getAttendees().toString());
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
        List<Event> events = new ArrayList<Event>();

        resultSet.moveToFirst();
/*
        for(int i = 0; i < resultSet.getCount(); i++){
            Event event = getEventFromCursor(resultSet);
            Log.i(TAG, "" + event.getID());
            events.add(event);
            resultSet.moveToNext();
        }*/

        do{
            Event event = getEventFromCursor(resultSet);
            Log.i(TAG, "" + event.getID());
            events.add(event);
        }while(resultSet.moveToNext());

        /*
        do{
            Event event = getEventFromCursor(resultSet);
            events.add(event);
        }while(resultSet.moveToNext());*/
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
        event.setDate(stringToDate(resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_DATE))));

        double loc_lat = resultSet.getDouble(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_LOCLAT));
        double loc_long = resultSet.getDouble(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_LOCLONG));
        event.setLocation(loc_lat, loc_long);
        event.setDuration(resultSet.getInt(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_DURATION)));
        return event;
    }


    /**
     * Convertes a string into a date object
     * @param dateString The string to be converted; should be in format: 'yyyy/MM/dd HH:mm:ss'
     * @return returns date object converted from string, or null if conversion failed
     */
    public static Date stringToDate(String dateString){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date;
        try{
            date = sdf.parse(dateString);
            return date;
        }catch (ParseException e){
            e.printStackTrace();
        }
        return null;
    }








    /*


    TESTING METHODS FOR EVENTSDATASOURCE,EVENTS AND DATABASE INTERACTION


    */

    /**
     * Put this in constructor, and call a test inside its body after creating properties
     *
    private void performTests() {
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        //insertData();
        //insertData();
        //insertData();
        createTestProperties();

        //tests
        //EDSTEST();
        createEventTEST();
        //deleteEventTEST();
        //getEventTEST();
    }

    private void createTestProperties(){
        testArray.add("Sarge");
        testArray.add("Church");
        testArray.add("Tucker");
        testArray.add("Caboose");
    }

    private void createEventTEST(){
        Log.i(TAG, "<----------------------RUNNING CREATE EVENT TEST------------------>");

        Date date = new Date();

        testEvent = createEvent("RvB Title Test", date,"Blood Gulch", testArray,"The Meta notes");
        testEvent.printEvent();

        //build record pairs
        /*ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ID, testEvent.getID());
        values.put(MySQLiteHelper.COLUMN_TITLE, testEvent.getTitle());
        values.put(MySQLiteHelper.COLUMN_DATE, testEvent.getDate());
        values.put(MySQLiteHelper.COLUMN_LOCATION, testEvent.getLocation());
        values.put(MySQLiteHelper.COLUMN_ATTENDEES,testEvent.getAttendees().toString());
        values.put(MySQLiteHelper.COLUMN_NOTES, testEvent.getNotes());

        deleteEvent(testEvent);

        Event testEvent2 = createEvent(values);

        testEvent2.printEvent();

        testEvent2.setNotes("event updates notes");
        updateEvent(testEvent2);
        Event testE = findEventByID(testEvent2.getID());

        testE.printEvent();
    }

    private void deleteEventTEST(){
        Date date = new Date();
        testEvent = createEvent("textTest", date,"testLocation", testArray,"eventNotes");
        Log.i(TAG, "New event with ID " + testEvent.getID() + " has been created and added to the database");
        testEvent.printEvent();
        deleteEvent(testEvent);

        //should fail
        Event testEvent2 = findEventByID(testEvent.getID());
    }

    private void getEventTEST(){
        Date date = new Date();
        testEvent = createEvent("textTest", date,"testLocation", testArray,"eventNotes");
        Log.i(TAG, "New event with ID " + testEvent.getID() + " has been created and added to the database");
        testEvent.printEvent();
        Event event = findEventByID(testEvent.getID());
        event.printEvent();

    }

    private void EDSTEST( ){
        Log.i(TAG, "----------------------RUNNING EDSTEST()------------------");
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }

        long id = insertData();
        Log.i(TAG, "sql insert successful!");

        Cursor resultSet = database.rawQuery(getData(), null);
        resultSet.moveToFirst();
        do{
            int eventID = resultSet.getInt(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_ID));
            String eventName = resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_TITLE));
            String eventDate = resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_DATE));
            String eventLocation = resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_LOCATION));
            ArrayList<String> eventAttendees = parseAttendees(resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_ATTENDEES)));
            String eventNotes = resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_NOTES));
            Log.i(TAG, "sql query successful!");

            Log.i(TAG, eventID + " \n"
                    + eventName + " \n"
                    + eventDate + " \n"
                    + eventLocation + " \n"
                    + eventAttendees + " \n"
                    + eventNotes + " \n");


        }
        while(resultSet.moveToNext());
        long size = new File(database.getPath()).length();
        Log.i(TAG, "Database currently has " + MySQLiteHelper.DATABASE_SIZE
                + " records");
        Log.i(TAG, "Size of database is currently: " + size);
        close();
    }*/

    /**
     * Inserts generic data for testing
     * @return
     */
    private long insertData(){
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("Hami");
        arr.add("Alex");
        arr.add("Jas");
        arr.add("Tina");
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, "textTest");
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        values.put(MySQLiteHelper.COLUMN_DATE, dateFormat.format(date));
        values.put(MySQLiteHelper.COLUMN_LOCLAT, 49);
        values.put(MySQLiteHelper.COLUMN_LOCLONG, -123);
        values.put(MySQLiteHelper.COLUMN_DURATION, 6);
        long id = database.insert(MySQLiteHelper.TABLE_EVENTS,null,values);
        close();
        return id;
    }


    /**
     * Creates raw sql query for all records in table
     * @return
     *
    public String getData(){
        String sqlCmd = "select * from " + MySQLiteHelper.TABLE_EVENTS;
        Log.i(TAG, "getData sql query created!!");
        return sqlCmd;
    }*/

}
