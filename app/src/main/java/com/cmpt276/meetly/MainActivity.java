package com.cmpt276.meetly;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;


/**
 * Holds the EventList fragment, and provides actionbar functionality like adding an event
 * Actionbar: Add event button, Location info, Location change button?
 */
public class MainActivity extends MaterialNavigationDrawer implements EventList.OnFragmentInteractionListener{

    private final String TAG = "MainActivity";
    private EventList eventListFragment;
    private final IntentFilter intentFilter = new IntentFilter();
    private BroadcastReceiver mReceiver;
    private WifiP2pHelper wifiP2pHelper;
    private Menu actionBarMenu;
    private MaterialAccount account;
    private MaterialSection eventListSection;
    private MyTimerTask serverEventSynchTask;
    private boolean locationSet = false;
    private String locationString;
    private EventUpdateObserver observer;


    @Override
    public void init(Bundle bundle) {
        Meetly.setMeetlySharedPrefs(getApplicationContext());
        Meetly.showPrefs(getApplicationContext());

        eventListFragment = (EventList) getFragmentManager().findFragmentByTag("EventListFragment");

        wifiP2pHelper = new WifiP2pHelper(this, getApplicationContext(), intentFilter);
        mReceiver = wifiP2pHelper.getReceiver();

        SharedPreferences preferences = getSharedPreferences(Meetly.MEETLY_PREFERENCES, MODE_PRIVATE);

        String username = getUsername(preferences);
        makeAccountSection(username);

        eventListSection = newSection(getString(R.string.app_name), EventList.newInstance());
        this.addSection(eventListSection);

        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        MaterialSection bottomSection = newSection(getString(R.string.action_settings), R.drawable.ic_settings_grey, settingsIntent);
        this.addBottomSection(bottomSection);

        allowArrowAnimation();
        setBackPattern(MaterialNavigationDrawer.BACKPATTERN_CUSTOM);

        if (!preferences.getBoolean(Meetly.MEETLY_PREFERENCES_FIRSTRUN, true)) {
            disableLearningPattern();
        }


        setSchedule();
    }

    protected void setEventUpdateObserver(EventUpdateObserver observer) {
        this.observer = observer;
    }

    @Override
    protected MaterialSection backToSection(MaterialSection currentSection) {
        if(currentSection == eventListSection) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            return currentSection;
        }
        return super.backToSection(currentSection);
    }


    private String getUsername(SharedPreferences preferences) {
        boolean loggedIn = preferences.getBoolean(Meetly.MEETLY_PREFERENCES_ISLOGGEDIN, false);
        String username;
        if (loggedIn) {
            username = preferences.getString(Meetly.MEETLY_PREFERENCES_USERNAME, "");
        }
        else {
            username = getString(R.string.main_not_logged_in);
        }
        return username;
    }

    private void makeAccountSection(String username) {
        account = new MaterialAccount(getResources(),
                username, "", R.drawable.card_picture_pizza, R.drawable.card_picture_default);
        this.addAccount(account);

        MaterialSection accountSection;
        if (username.equals(getString(R.string.main_not_logged_in))) {
            accountSection = newSection(getString(R.string.app_login), R.drawable.ic_add_grey, new Intent(MainActivity.this, LoginActivity.class));
            this.addAccountSection(accountSection);
        }
        else {
            accountSection = newSection(getString(R.string.app_logout), R.drawable.ic_delete_grey, new MaterialSectionListener() {
                @Override
                public void onClick(MaterialSection section) {
                    showLogOut();
                }
            });
            this.addAccountSection(accountSection);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        actionBarMenu = menu;
        if (locationSet) {
            setUserLocationButton();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_db) {
            onUpgradeDBClick(getCurrentFocus());
            return true;
        }
        else if (id == R.id.action_get_location) {
            setUserLocationButton();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUserLocationButton() {
        MenuItem menuItem = actionBarMenu.findItem(R.id.action_get_location);
        if (locationString == null) {
            locationString = UserLocation.getLocation(this);
        }
        setLocationString(menuItem, locationString);
    }

    private void setLocationString(MenuItem menuItem, String location) {
        if (!location.equals(getString(R.string.no_location_found))) {
            menuItem.setIcon(null);
            menuItem.setTitle(location);
        }
        else {
            menuItem.setIcon(null);
            menuItem.setTitle(getString(R.string.location_not_available));
        }
        locationSet = true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    /* Switches to Login activity */
    private void goToLoginScreen(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /*
     * Shows option of logging user out of Meetly
     */
    public void showLogOut(){
        this.closeDrawer();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_logout_confirmation));
        builder.setPositiveButton(getString(R.string.app_logout_dialog_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                logOut();
            }
        });
        builder.setNegativeButton(getString(R.string.app_logout_dialog_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*
     * Removes current username and token preferences from app memory
     */
    private void logOut(){
        SharedPreferences settings = getSharedPreferences(Meetly.MEETLY_PREFERENCES,  MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Meetly.MEETLY_PREFERENCES_USERNAME, getResources().getString(R.string.main_defaultLoginMessage));
        editor.putBoolean(Meetly.MEETLY_PREFERENCES_ISLOGGEDIN, false);
        editor.putInt(Meetly.MEETLY_PREFERENCES_USERTOKEN, -1);
        editor.commit();
        Toast.makeText(getApplicationContext(), "You have been successfully logged out", Toast.LENGTH_LONG).show();
        // Refresh main activity upon close of dialog box
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
        this.finish(); //
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
        //dbHelper.onUpgrade(database, MySQLiteHelper.DATABASE_VERSION, MySQLiteHelper.DATABASE_VERSION + 1);
        MySQLiteHelper.deleteDatabase(database, getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
        setSchedule();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //For Scheduling Time-Interval Event Retrieval from Server

    private void setSchedule(){
        //For timer
        serverEventSynchTask = new MyTimerTask();
        Timer synchTimer = new Timer();
//        public void schedule (TimerTask task, long delay, long period)
//        Schedule a task for repeated fixed-delay execution after a specific delay.
//
//        Parameters
//        task  the task to schedule.
//        delay  amount of time in milliseconds before first execution.
//        period  amount of time in milliseconds between subsequent executions.

        Long syncInterval = getServerSyncInterval();

        if (syncInterval > 0) {
            synchTimer.schedule(serverEventSynchTask, 3000, syncInterval);
        }
    }

    private long getServerSyncInterval() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return Long.parseLong(preferences.getString("server_sync_interval", "0"));
    }

    protected void syncWithServerNow() {
        new MyTimerTask().run();
    }

    class MyTimerTask extends TimerTask {
        public void run() {
            MeetlyServer server = new MeetlyServer();
            SharedPreferences settingss = getSharedPreferences(Meetly.MEETLY_PREFERENCES, MODE_PRIVATE);
            EventsDataSource eventsDataSource = new EventsDataSource(getApplicationContext());
            if(settingss.getBoolean(Meetly.MEETLY_PREFERENCES_ISLOGGEDIN, false)){
                try {
                    int token = server.login("fritter@sfu.ca", "password");
                    for (MeetlyServer.MeetlyEvent e : server.fetchEventsAfter(10)) {
                        Log.i("DBTester", "Event " + e.title);

                        //make sure event is not already in the database
                        Event event = null;
                        try{
                            event = eventsDataSource.findEventBySharedID(e.eventID);
                        }catch (RuntimeException exc){
                            exc.printStackTrace();
                        }
                        if(event == null){
                            //event is not, so add it
                            LatLng latLng = new LatLng(e.latitude,e.longitude);
                            Event event2 = eventsDataSource.createEvent(e.title,e.startTime,e.endTime,latLng);
                            event2.setSharedEventID(e.eventID);
                            event2.setViewed(false);
                        }else{
                            //event is already in the database, so just update it
                            event.setTitle(e.title);
                            event.setStartDate(e.startTime);
                            event.setEndDate(e.endTime);
                            LatLng latLng = new LatLng(e.latitude,e.longitude);
                            event.setLocation(latLng);
                            eventsDataSource.updateDatabaseEvent(event);
                        }
                    }
                } catch (MeetlyServer.FailedFetchException e) {
                    e.printStackTrace();
                } catch (IMeetlyServer.FailedLoginException e) {
                    Log.i(TAG, "Failed to login");
                    e.printStackTrace();
                }

                Calendar currentCal = Calendar.getInstance();
                Log.i(TAG, "Fetched new events @: " + Event.EVENT_DATEFORMAT.format(currentCal.getTime()));
                observer.eventsUpdated();
            }else{
                Log.i(TAG, "Failed to fetch new events. User not logged in");

            }

        }
    }
}
