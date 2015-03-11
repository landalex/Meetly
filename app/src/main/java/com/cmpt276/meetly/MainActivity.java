package com.cmpt276.meetly;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            onDeleteDBClick(getCurrentFocus());
            return true;
        } else if (id == R.id.action_add_event) {
            if (newDS.createEvent("Test",
                    new Date(),
                    "A Place",
                    new ArrayList<String>(),
                    "This is a note") != null) {
                Log.i(TAG, "Event created");
            }
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
