package com.cmpt276.meetly;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
        EventsDataSource database = new EventsDataSource(this);
        event = database.findEventByID(1);

        // Getting a reference to all our fields
        TextView eventName = (TextView) findViewById(R.id.editEventName);
        Button changeEventTimeBtn = (Button) findViewById(R.id.editChooseTime);
        Button changeEventDateBtn = (Button) findViewById(R.id.editChooseDate);
        TextView eventDuration = (TextView) findViewById(R.id.editDurationField);

        eventName.setText(event.getTitle());
        changeEventTimeBtn.setText(event.getDate().getHours() + ":" + event.getDate().getMinutes());
        changeEventDateBtn.setText(event.getDate().getMonth() + "/" + event.getDate().getDay() + "/" + event.getDate().getYear());
        eventDuration.setText(event.getDuration());

    }

}
