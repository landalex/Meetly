package com.cmpt276.meetly;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Collection;
import java.util.Iterator;

import de.keyboardsurfer.android.widget.crouton.Crouton;


/**
 * Holds the EventList fragment, and provides actionbar functionality like adding an event
 * Actionbar: Add event button, Location info, Location change button?
 */
public class MainActivity extends ActionBarActivity implements EventList.OnFragmentInteractionListener{

    private final String TAG = "MainActivity";
    private EventList eventListFragment;
    private Crouton locationCrouton;
    private final IntentFilter intentFilter = new IntentFilter();
    private boolean wifiP2pEnabled;
    private BroadcastReceiver mReceiver;
    private WifiP2pHelper wifiP2pHelper;
    private WifiP2pDeviceList wifiPeers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiP2pHelper = new WifiP2pHelper(this, getApplicationContext(), intentFilter);
        mReceiver = wifiP2pHelper.getReceiver();

        setMeetlySharedPrefs();
        openFragment(getCurrentFocus());

        wifiP2pHelper.getWifiPeers();
        wifiPeers = wifiP2pHelper.getPeersList();


        connectionTest();

        }

    protected void setIsWifiP2pEnabled(boolean wifiP2pEnabled) {
        this.wifiP2pEnabled = wifiP2pEnabled;
    }

    public void connectionTest() {
        WifiP2pDevice device = wifiPeers.getDeviceList().iterator().next();
        if (device != null) {
            wifiP2pHelper.connectToPeer(device);
        }
    }

    private void setMeetlySharedPrefs() {
        SharedPreferences settings = getSharedPreferences(Meetly.MEETLY_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Log.i(TAG, "First run?: " + settings.getBoolean(Meetly.MEETLY_PREFERENCES_FIRSTRUN, false));
        if(!settings.getBoolean(Meetly.MEETLY_PREFERENCES_FIRSTRUN, false)){
            editor.putInt(Meetly.MEETLY_PREFERENCES_USERTOKEN, -1);
            editor.putString(Meetly.MEETLY_PREFERENCES_USERNAME, "Not Logged In");
            editor.putBoolean(Meetly.MEETLY_PREFERENCES_FIRSTRUN, true);
            editor.commit();
        }
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
            onUpgradeDBClick(getCurrentFocus());
            return true;
        } else if (id == R.id.action_add_event) {
            Intent intent = new Intent(this, CreateEvent.class);
            startActivity(intent);
        } else if (id == R.id.action_login){
            Intent intent = new Intent(this, LoginActivity.class);
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

    /* For opening event list on MainActivity */
    public void openFragment(View view) {
        Meetly.showPrefs(getApplicationContext());
        getFragmentManager().beginTransaction().replace(android.R.id.content, EventList.newInstance(), "EventListFragment").commit();
    }

    /* For Upgrade of database*/
    public void onUpgradeDBClick(View view){
        MySQLiteHelper dbHelper = new MySQLiteHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(database,MySQLiteHelper.DATABASE_VERSION,MySQLiteHelper.DATABASE_VERSION+1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }
}
