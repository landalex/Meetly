package com.cmpt276.meetly;

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

import de.keyboardsurfer.android.widget.crouton.Crouton;


/**
 * Actionbar: Add event button, Location info, Location change button?
 */
public class MainActivity extends ActionBarActivity implements EventList.OnFragmentInteractionListener{

    private final String TAG = "MainActivity";
    private EventsDataSource newDS;
    private EventList eventListFragment;
    private Crouton locationCrouton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newDS = new EventsDataSource(getApplicationContext());
        openFragment(getCurrentFocus());

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
        if (id == R.id.action_delete_db) {
            onDeleteDBClick(getCurrentFocus());
            return true;
        } else if (id == R.id.action_add_event) {
            Intent intent = new Intent(this, CreateEvent.class);
            startActivity(intent);
        } else if (id == R.id.action_get_location) {
            if (eventListFragment == null) {
                eventListFragment = (EventList) getFragmentManager().findFragmentByTag("EventListFragment");
                locationCrouton = eventListFragment.makeLocationCrouton();
            }
            if (eventListFragment.showingCrouton) {
                locationCrouton.hide();
                Log.d(TAG, "hide crouton");
            }
            else {
                locationCrouton = eventListFragment.makeLocationCrouton();
                locationCrouton.show();
                Log.d(TAG, "show crouton");
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    public void goToViewEvent(){
        EventsDataSource eds = new EventsDataSource(getApplicationContext());

        Event someEvent = eds.findEventByID(5);
        Intent intent = new Intent(getApplicationContext(),ViewEvent.class);
        intent.putExtra("eventID",someEvent.getID());
        startActivity(intent);
    }

    /* For opening event list on MainActivity */
    public void openFragment(View view) {
        getFragmentManager().beginTransaction().replace(android.R.id.content, EventList.newInstance(), "EventListFragment").commit();
    }

    /* For QuickDelete of database*/
    public void onDeleteDBClick(View view){
        MySQLiteHelper dbHelper = new MySQLiteHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.close();
        dbHelper.deleteDatabase(database, getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }
}
