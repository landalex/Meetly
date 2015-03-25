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

public class Meetly extends ActionBarActivity {

    private static final String TAG = "ParentActivity";

    //shared preferences
    public static final String MEETLY_PREFERENCES = "Meetly_Prefs";
    public static final String MEETLY_PREFERENCES_USERTOKEN = "user_token"; //int
    public static final String MEETLY_PREFERENCES_FIRSTRUN = "first_run"; //boolean
    public static final String MEETLY_PREFERENCES_USERNAME = "username"; //string
    public static final String defaultUMessage = "not logged in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
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



























