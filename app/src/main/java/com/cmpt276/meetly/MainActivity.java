package com.cmpt276.meetly;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.database.sqlite.SQLiteDatabase;


/**
 * Actionbar: Add event button, Location info, Location change button?
 */
public class MainActivity extends ActionBarActivity implements EventList.OnFragmentInteractionListener{

    private final String TAG = "MainActivity";
    private MySQLiteHelper dbHelper;
    private SQLiteDatabase database;
    private String[] dbColumns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_TITLE};
    //testing database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new MySQLiteHelper(getApplicationContext());
        database = dbHelper.getWritableDatabase();

        database.execSQL(insertData());
        Log.i(TAG, "sql insert successful!");


        Cursor resultSet = database.rawQuery(getData(), null);
        resultSet.moveToFirst();
        int eventID = resultSet.getInt(1);
        String eventName = resultSet.getString(1);
        String eventDate = resultSet.getString(2);
        String eventLocation = resultSet.getString(3);
        String eventAttendees = resultSet.getString(4);
        String eventNotes = resultSet.getString(5);
        Log.i(TAG, "sql query successful!");

        Log.i(TAG, eventName + " \n"
                + eventDate + " \n"
                + eventLocation + " \n"
                + eventAttendees + " \n"
                + eventNotes + " \n");

        //Log.i(TAG, dbHelper.execSQL(getData()));


        //getFragmentManager().beginTransaction().replace(android.R.id.content, new EventList()).commit();
    }

    public String insertData(){
        String sqlCmd = "insert into " + MySQLiteHelper.TABLE_EVENTS
                + " values (2,'textTest','dateTest','locationTest','AttendeesTest','notesTest');";
        Log.i(TAG, "insertData sql query created!");
        return sqlCmd;
    }

    public String getData(){
        String sqlCmd = "select * from " + MySQLiteHelper.TABLE_EVENTS + " where _id=1;";
        Log.i(TAG, "getData sql query created!!");
        return sqlCmd;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
