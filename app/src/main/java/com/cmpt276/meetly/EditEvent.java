package com.cmpt276.meetly;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;


public class EditEvent extends Activity {
    Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Getting a reference to the event thats passed in
        Intent in = getIntent();
        Bundle extras = in.getExtras();
        long id = extras.getLong("eventID");

        EventsDataSource database = new EventsDataSource(this);
        event = database.findEventByID(id);

        // Getting a reference to all our fields
        TextView eventName = (TextView) findViewById(R.id.editEventName);
        TextView eventDate = (TextView) findViewById(R.id.currentEventDate);
        Button changeEventTimeBtn = (Button) findViewById(R.id.editChooseTime);
        Button changeEventDateBtn = (Button) findViewById(R.id.editChooseDate);
        TextView eventDuration = (TextView) findViewById(R.id.editDurationField);

        eventName.setText(event.getTitle());
        eventDate.setText(event.getDate().toString());
        eventDuration.setText("" + event.getDuration());

    }

}
