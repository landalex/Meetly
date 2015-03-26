package com.cmpt276.meetly;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.util.Log;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * A test server implementation to simulate interacting with meetly server
 */
public class MeetlyTestServer implements IMeetlyServer{

    final private String TAG = "MeetlyTestServer";

    private MySQLiteHelper dbHelper;
    private SQLiteDatabase database;
    private String[] dbColumns = {MySQLiteHelper.COLUMN_UID,
            MySQLiteHelper.COLUMN_USERNAME,
            MySQLiteHelper.COLUMN_PASS};

    private Context context;

    //test credentials
    //username: admin@sfu.ca
    //password: password

    /**
     * Logs a user into the central server. This method takes a username and
     * a password. If the user already exists in the data base then:
     *   1) If the password is correct for the user, the user is logged in
     *     and a token for the user is returned.
     *   2) If the password is incorrect for the user, a FailedLoginException is
     *     thrown.
     * If the user does not already exist in the database, then the user is
     * created with the given password, and the a token for the user is
     * returned.
     * @param username
     * @param password
     * @return integer token for the user
     * @throws FailedLoginException
     */
    public int login(String username, String password, Context context) throws FailedLoginException{
        //TODO: remove context for final
        Log.i(TAG, "Attempting menu_login to MeetlyTestServer...");
        dbHelper = new MySQLiteHelper(context);;
        database = dbHelper.getWritableDatabase();
        Cursor resultSet;


        //first, check if account exists
        try{
            resultSet = database.query(MySQLiteHelper.TABLE_USERS,dbColumns,
                    MySQLiteHelper.COLUMN_USERNAME + " = '" + username + "'", null, null, null,null);
        }catch (RuntimeException e){
            e.printStackTrace();
            return -1;
        }


        Log.i(TAG, "Username matched " + resultSet.getCount() + " accounts on server");

        //if found too many accounts with that username, throw error
        if (resultSet.getCount() > 1){
            Log.e(TAG, "too many accounts with that username found. Report problem...");
            throw new RuntimeException("too many accounts with that username found...");

        //if account exists, validate password
        }else if(resultSet.getCount() > 0){
            Log.i(TAG, "Username account found! Checking password....");
            resultSet.moveToFirst();
            String correctPassword = resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_PASS));
            if(password.equals(correctPassword)){
                return (int) resultSet.getLong(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_UID));
            }else{
                throw new FailedLoginException("Password was incorrect");
            }

        //if account does not exist, create it
        }else{
            Log.i(TAG, "Account does not exist. Creating new account...");

            ContentValues values = new ContentValues();

            Log.i(TAG, "Adding given credentials...");
            values.put(MySQLiteHelper.COLUMN_USERNAME, username);
            values.put(MySQLiteHelper.COLUMN_PASS, password);
            long insertID = database.insert(MySQLiteHelper.TABLE_USERS, null, values);

            resultSet = database.query(MySQLiteHelper.TABLE_USERS,dbColumns, MySQLiteHelper.COLUMN_UID + " = " + insertID, null, null, null,null);

            resultSet.moveToFirst();
            Log.i(TAG, "username: " + resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_USERNAME))
                    + " with password: " + resultSet.getString(resultSet.getColumnIndex(MySQLiteHelper.COLUMN_PASS))
                    + " has been added to the database");

            return (int) insertID;
        }

    }

    /**
     * Publishes an event to the central server. Takes all relevant data for an
     * event and publishes that event on the central server. If the publication
     * fails for any reason, a FailedPublicationException is thrown. If the
     * event is successfully published, a unique ID for the event is returned.
     * @param username
     * @param userToken
     * @param title
     * @param startTime
     * @param endTime
     * @param latitude
     * @param longitude
     * @return
     * @throws FailedPublicationException
     */
    public int publishEvent(String username, int userToken,String title, Calendar startTime,
                            Calendar endTime, double latitude, double longitude) throws FailedPublicationException{

        EventsDataSource EDS = new EventsDataSource(context);




        throw new FailedPublicationException();
    }


    /**
     * Modifies an existing event. Given the relevant details for an event,
     * modifies the existing event on the central server. If publication of the
     * modified event fails for any reason, a FailedPublicationException is
     * thrown. If the event is successfully published, the method terminates
     * normally.
     * @param eventID
     * @param userToken
     * @param title
     * @param startTime
     * @param endTime
     * @param latitude
     * @param longitude
     * @throws FailedPublicationException
     */
    public void modifyEvent(int eventID, int userToken, String title, Calendar startTime,
                            Calendar endTime,double latitude, double longitude) throws FailedPublicationException{
        EventsDataSource EDS = new EventsDataSource(context);


        throw new FailedPublicationException();
    }



    class FailedLoginException extends RuntimeException {

        public FailedLoginException(){
            super();
        }

        public FailedLoginException(String message){
            super(message);
        }

        public String getMessage(){
            return super.getMessage();
        }
    };

    class FailedPublicationException extends RuntimeException {
        public FailedPublicationException(){
            super();
        }

        public FailedPublicationException(String message){
            super(message);
        }

        public String getMessage(){
            return super.getMessage();
        }
    }

}
