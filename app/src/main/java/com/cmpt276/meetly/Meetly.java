package com.cmpt276.meetly;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Meetly class to be inherited from
 */

/**
 * TODO: make sure month is updated to database (incorrect formatting?)
 * TODO: implement start and end date to get duration
 * TODO: add sharedEventID to Event
 */

public class Meetly extends ActionBarActivity {

    private static final String TAG = "ParentActivity";

    //shared preferences
    public static final String MEETLY_PREFERENCES = "Meetly_Prefs";
    public static final String MEETLY_PREFERENCES_USERTOKEN = "user_token"; //int
    public static final String MEETLY_PREFERENCES_FIRSTRUN = "first_run"; //boolean
    public static final String MEETLY_PREFERENCES_USERNAME = "username"; //string
    public static final String MEETLY_PREFERENCES_ISLOGGEDIN = "is_logged_in"; //string
    public static final String defaultUMessage = "not logged in"; //boolean

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
    }

    /**
     * Sets shared preferences for meetly application
     */
    public static void setMeetlySharedPrefs(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Meetly.MEETLY_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Log.i(TAG, "First run?: " + settings.getBoolean(Meetly.MEETLY_PREFERENCES_FIRSTRUN, true));

        if(settings.getBoolean(Meetly.MEETLY_PREFERENCES_FIRSTRUN, true)){
            editor.putInt(Meetly.MEETLY_PREFERENCES_USERTOKEN, -1);
            editor.putString(Meetly.MEETLY_PREFERENCES_USERNAME, defaultUMessage);
            editor.putBoolean(Meetly.MEETLY_PREFERENCES_FIRSTRUN, false);
            editor.putBoolean(Meetly.MEETLY_PREFERENCES_ISLOGGEDIN, false);
            editor.commit();
        }
    }

    /**
     * Prints currently logged in username to Toast and Log.i
     * If no one is logged in, displays default message
     * @param context application context invoked from
     */
    public static void showPrefs(Context context){
        SharedPreferences settings = context.getSharedPreferences(Meetly.MEETLY_PREFERENCES, MODE_PRIVATE);
        Log.i(TAG, "Currently logged in as: " + settings.getString(Meetly.MEETLY_PREFERENCES_USERNAME, defaultUMessage)
                + " with token: " + settings.getInt(Meetly.MEETLY_PREFERENCES_USERTOKEN, -1));

        Toast.makeText(context, "Hello, " + settings.getString(Meetly.MEETLY_PREFERENCES_USERNAME, defaultUMessage)
                , Toast.LENGTH_SHORT).show();

    }

}



























