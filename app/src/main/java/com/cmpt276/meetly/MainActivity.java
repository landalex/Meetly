package com.cmpt276.meetly;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;


/**
 * Holds the EventList fragment, and provides actionbar functionality like adding an event
 * Actionbar: Add event button, Location info, Location change button?
 */
public class MainActivity extends MaterialNavigationDrawer implements EventList.OnFragmentInteractionListener{

    private final String TAG = "MainActivity";
    private EventList eventListFragment;
    private Crouton locationCrouton;
    private final IntentFilter intentFilter = new IntentFilter();
    private BroadcastReceiver mReceiver;
    private WifiP2pHelper wifiP2pHelper;
    private Menu actionBarMenu;


    @Override
    public void init(Bundle bundle) {
        Meetly.setMeetlySharedPrefs(getApplicationContext());
        Meetly.showPrefs(getApplicationContext());

        wifiP2pHelper = new WifiP2pHelper(this, getApplicationContext(), intentFilter);
        mReceiver = wifiP2pHelper.getReceiver();

        SharedPreferences preferences = getSharedPreferences(Meetly.MEETLY_PREFERENCES, MODE_PRIVATE);

//        openFragment(getCurrentFocus());
//        Fragment eventList = getFragmentManager().findFragmentByTag("EventListFragment");
        MaterialAccount account = new MaterialAccount(getResources(),
                preferences.getString(Meetly.MEETLY_PREFERENCES_USERNAME, getString(R.string.main_defaultLoginMessage)),
                "", R.drawable.card_picture_pizza, R.drawable.card_picture_default);
        this.addAccount(account);

        MaterialSection mainSection = newSection(getString(R.string.app_name), EventList.newInstance());
        this.addSection(mainSection);

        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        MaterialSection bottomSection = newSection(getString(R.string.action_settings), R.drawable.ic_settings_grey, settingsIntent);
        this.addBottomSection(bottomSection);

        allowArrowAnimation();

        if (!preferences.getBoolean(Meetly.MEETLY_PREFERENCES_FIRSTRUN, true)) {
            disableLearningPattern();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        actionBarMenu = menu;
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
        } else if (id == R.id.action_login){
            SharedPreferences settings = getSharedPreferences(Meetly.MEETLY_PREFERENCES, MODE_PRIVATE);
            boolean isLoggedIn = settings.getBoolean(Meetly.MEETLY_PREFERENCES_ISLOGGEDIN, false);

            //if logged in, ask user if they want to log out
            if(isLoggedIn){
                showLogOut();
            }else{
                goToLoginScreen();
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

    /*
     * Changes the menu_login menu item text depending if user is logged in or not
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        SharedPreferences settings = getSharedPreferences(Meetly.MEETLY_PREFERENCES, MODE_PRIVATE);
        boolean isLoggedIn = settings.getBoolean(Meetly.MEETLY_PREFERENCES_ISLOGGEDIN, false);

        MenuItem menuItem = actionBarMenu.findItem(R.id.action_login);

        //if logged in, show user name
        if(isLoggedIn) {
            menuItem = actionBarMenu.findItem(R.id.action_login);
            String menuString = (getResources().getText(R.string.main_loggedin) + " " + settings.getString(Meetly.MEETLY_PREFERENCES_USERNAME, getResources().getText(R.string.main_defaultLoginMessage).toString()));
            menuItem.setTitle(menuString);

        } else {
            //show default menu_login message
            menuItem.setTitle(getResources().getString(R.string.app_login));

            //turn off popupMenu for logging out
//            findViewById(R.id.logOut).setClickable(false);
        }
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
        PopupMenu popupMenu = new PopupMenu(this,findViewById(R.id.logOut));
        // This activity implements OnMenuItemClickListener
        popupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener(){
                     @Override
                     public boolean onMenuItemClick(MenuItem item) {
                         logOut();
                         return true;
                     }
                 }
        );
        //MenuInflater inflater = popupMenu.getMenuInflater();
        //inflater.inflate(R.menu.menu_login, popupMenu.getMenu());
        popupMenu.inflate(R.menu.menu_login);
        popupMenu.show();
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
        //dbHelper.onUpgrade(database,MySQLiteHelper.DATABASE_VERSION,MySQLiteHelper.DATABASE_VERSION+1);
        MySQLiteHelper.deleteDatabase(database, getApplicationContext());
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

//    @Override
//    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Intent startMain = new Intent(Intent.ACTION_MAIN);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(startMain);
//        }
//        return true;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

}
