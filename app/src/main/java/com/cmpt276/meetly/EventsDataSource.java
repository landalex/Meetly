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


public class EventsDataSource {


    final private String TAG = "EventsDataSource";

    private MySQLiteHelper dbHelper;
    private SQLiteDatabase database;
    private String[] dbColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_TITLE,
            MySQLiteHelper.COLUMN_DATE,
            MySQLiteHelper.COLUMN_LOCATION,
            MySQLiteHelper.COLUMN_ATTENDEES,
            MySQLiteHelper.COLUMN_NOTES};

    //Testing properties
    private ArrayList<String> testArray = new ArrayList<String>();
    Event testEvent;





    /**
     * Events Data Source constructor.
     * Facilitates database connections and supports adding new events and fetching events
     * @param context Application context
     */
    public EventsDataSource(Context context){
        dbHelper = new MySQLiteHelper(context);
        performTests();
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
     * @param attendees attendees for event
     * @param location location  for event
     * @param notes notes  for event
     */
    public Event createEvent(String title, Date date, String location, ArrayList<String> attendees, String notes) {
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        //build record pairs
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, title);
        values.put(MySQLiteHelper.COLUMN_DATE, date.toString());
        values.put(MySQLiteHelper.COLUMN_LOCATION, location);
        values.put(MySQLiteHelper.COLUMN_ATTENDEES,attendees.toString());
        values.put(MySQLiteHelper.COLUMN_NOTES, notes);

        //get row id and insert into database
        long insertID = database.insert(MySQLiteHelper.TABLE_EVENTS,null,values);

        //get this record and create new event object
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns,
                MySQLiteHelper.COLUMN_ID + " = " + insertID, null,null,null,null);
        resultSet.moveToFirst();
        Event event = new Event(insertID,title, date, location, attendees, notes);
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

        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns,MySQLiteHelper.COLUMN_ID + " = " + eventID,null,null,null,null);

        if(resultSet.getCount() == 0){
            Log.e(TAG, "Error attempting to retrieve record from database. The ID \"" + eventID + "\" does not match any record in the database");
            return null;
        }
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, MySQLiteHelper.COLUMN_DATE
                + " = " + dateFormat.format(date),null,null,null,"date");
        if(resultSet.getCount() == 0){
            Log.e(TAG, "Error attempting to retrieve records from database. There are not events on this date");
            return null;
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
    public List<Event> findEventsByLocation(String location){
        database = dbHelper.getReadableDatabase();
        List<Event> events;
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns,MySQLiteHelper.COLUMN_LOCATION
                + " = " + location,null,null,null,"date");

        if(resultSet.getCount() == 0){
            Log.e(TAG, "Error attempting to retrieve records from database. There are not events in this location");
            return null;
        }

        events = buildEventsList(resultSet);
        return events;
    }

    /**
     * Gets all the events in the database and returns them in a list of events
     * @return A list of events
     */
    public List<Event> getAllEvents(){
        database = dbHelper.getReadableDatabase();
        List<Event> events;
        Cursor resultSet = database.query(MySQLiteHelper.TABLE_EVENTS, dbColumns, null, null, null, null, null);
        events = buildEventsList(resultSet);
        resultSet.close();
        close();
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
        values.put(MySQLiteHelper.COLUMN_LOCATION, event.getLocation());
        values.put(MySQLiteHelper.COLUMN_ATTENDEES,event.getAttendees().toString());
        values.put(MySQLiteHelper.COLUMN_NOTES, event.getNotes());

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
        while(!resultSet.isAfterLast()){
            Event event = getEventFromCursor(resultSet);
            events.add(event);
            resultSet.moveToNext();
        }
        return events;
    }


    /**
     * Gets an Event from a cursor to this database
     * @param resultSet cursor to the database
     * @return The Event from the database cursor points to
     */
    private Event getEventFromCursor(Cursor resultSet){
        resultSet.moveToFirst();
        Event event = new Event();
        event.setID(resultSet.getLong(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_ID)));
        event.setTitle(resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_TITLE)));
        event.setDate(stringToDate(resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_DATE))));
        event.setLocation(resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_LOCATION)));
        event.setAttendees(parseAttendees(resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_ATTENDEES))));
        event.setNotes(resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_NOTES)));
        return event;
    }


    /**
     * Convertes a string into a date object
     * @param dateString The string to be converted; should be in format: 'yyyy/MM/dd HH:mm:ss'
     * @return returns date object converted from string, or null if conversion failed
     */
    private Date stringToDate(String dateString){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date;
        try{
            date = dateFormat.parse(dateString);
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
     */
    private void performTests() {
        try{
            open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        insertData();
        insertData();
        insertData();
        createTestProperties();

        //tests
        //EDSTEST();
        //createEventTEST();
        //deleteEventTEST();
        getEventTEST();
    }

    private void createTestProperties(){
        testArray.add("Zelda");
        testArray.add("Link");
        testArray.add("Shepard");
        testArray.add("Liar");
    }

    private void createEventTEST(){
        Log.i(TAG, "<----------------------RUNNING CREATE EVENT TEST------------------>");

        Date date = new Date();

        testEvent = createEvent("textTest", date,"testLocation", testArray,"eventNotes");

        Log.i(TAG, testEvent.getTitle() + " \n"
                + testEvent.getDate() + " \n"
                + testEvent.getLocation() + " \n"
                + testEvent.getAttendees() + " \n"
                + testEvent.getNotes() + " \n");
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
    }

    /**
     * Inserts generic data for testing
     * @return
     */
    private long insertData(){
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
        values.put(MySQLiteHelper.COLUMN_LOCATION, "locationTest");
        values.put(MySQLiteHelper.COLUMN_ATTENDEES,arr.toString());
        values.put(MySQLiteHelper.COLUMN_NOTES, "notesTest");
        return database.insert(MySQLiteHelper.TABLE_EVENTS,null,values);
    }


    /**
     * Creates raw sql query for all records in table
     * @return
     */
    public String getData(){
        String sqlCmd = "select * from " + MySQLiteHelper.TABLE_EVENTS;
        Log.i(TAG, "getData sql query created!!");
        return sqlCmd;
    }

}
