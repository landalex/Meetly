package com.cmpt276.meetly;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

/**
 * This class is responsible for creating the Meetly Database
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MeetlyDB";
    public static final int DATABASE_VERSION = 3;

    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SHAREDEVENTID = "sharedEventID";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_STARTDATE = "startDate";
    public static final String COLUMN_ENDDATE = "startDate";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    // database table sql statement for events
    private static final String DATABASE_CREATE = "create table "
            + TABLE_EVENTS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_SHAREDEVENTID + " integer, "
            + COLUMN_TITLE + " text not null,"
            + COLUMN_STARTDATE + " text,"
            + COLUMN_ENDDATE + " text,"
            + COLUMN_LATITUDE + " double,"
            + COLUMN_LONGITUDE + " double,"
            + ");";


    /**
     * Constructor
     * @param context
     */
    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        try{
            database.execSQL(DATABASE_CREATE);

        }catch (SQLiteException e){
            e.printStackTrace();
        }

    }

    /**
     * Deletes all existing data from the table and re-creates the table
     * @param db the database to re-create
     * @param oldVersion the current version of the database
     * @param newVersion the version of the database to move to
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        //onCreate(db);
    }

    /**
     * Closes connections to database and deletes it. This should be called if
     * changes are made to the database structure
     * @param db the database to close
     * @param context
     */
    public static void deleteDatabase(SQLiteDatabase db, Context context){
        db.close();
        context.deleteDatabase(DATABASE_NAME);
    }



}
