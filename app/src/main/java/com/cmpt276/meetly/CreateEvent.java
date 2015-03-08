package com.cmpt276.meetly;

import android.app.Activity;
import android.os.Bundle;

/**
 * Fields: Name, Location (Open map), Date/Time (Use the circle thingy), People (Open contacts), Notes (1000 characters?)
 */
public class CreateEvent extends Activity {

    private final String TAG = "CreateEventActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
    }

}
