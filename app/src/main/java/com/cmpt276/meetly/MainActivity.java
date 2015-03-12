package com.cmpt276.meetly;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;


/**
 * Actionbar: Add event button, Location info, Location change button?
 */
public class MainActivity extends ActionBarActivity implements EventList.OnFragmentInteractionListener{

    private final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goToViewEvent();
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

    public void goToViewEvent(){
        EventsDataSource eds = new EventsDataSource(getApplicationContext());

        Event someEvent = eds.findEventByID(5);
        Intent intent = new
        Intent(getApplicationContext(),ViewEvent.class);
        intent.putExtra("eventID",someEvent.getID());
        startActivity(intent);
    }

    /* For QuickDelete of database
    public void onDeleteDBClick(View view){
        MySQLiteHelper dbHelper = new MySQLiteHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.close();
        dbHelper.deleteDatabase(database, getApplicationContext());
    }*/
}
