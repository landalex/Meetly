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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EditEvent extends Activity {
    Event event;
    private Integer[] date = new Integer[]{2015, 1, 1};
    private Integer[] hourAndMinuteArray = new Integer[]{0, 0};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Getting a reference to the event thats passed in
        getEventToEdit();

        // Getting a reference to all our fields
        TextView eventName = (TextView) findViewById(R.id.editEventName);
        TextView eventDate = (TextView) findViewById(R.id.currentEventDate);
        Button changeEventTimeBtn = (Button) findViewById(R.id.editChooseTime);
        Button changeEventDateBtn = (Button) findViewById(R.id.editChooseDate);
        TextView eventDuration = (TextView) findViewById(R.id.editDurationField);

        // Setting the TextViews and Map to display the details of the event thats passed in.
        eventName.setText(event.getTitle());
        eventDate.setText(event.getDate().toString());
        eventDuration.setText("" + event.getDuration());
        Date newDate = event.getDate();
        // TODO: Allow event location to be changed

        // Save Button
        saveButton();

    }

    private void getEventToEdit() {
        Intent in = getIntent();
        Bundle extras = in.getExtras();
        long id = extras.getLong("eventID");

        EventsDataSource database = new EventsDataSource(this);
        event = database.findEventByID(id);
    }

    private void saveButton() {
        Button saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                event.setTitle(newTitle);
//                event.setDate(newDate);
//                etc
//                finish();
            }
        });
    }

    private Date formatEventTimeAndDate() {
        // If the month is single digit
        String tempMonth = leftPadDateOrTime(date[1]);

        // If the day is single digit
        String tempDay = leftPadDateOrTime(date[2]);

        // If the hour is single digit
        String tempHour = leftPadDateOrTime(hourAndMinuteArray[0]);

        // If the minute is single digit
        String tempMinute = leftPadDateOrTime(hourAndMinuteArray[1]);

        // yyyy - mm - dd <> hh:mm:ss
        String str = date[0] + "/" + tempMonth + "/" + tempDay + " " + tempHour + ":" + tempMinute + ":" + "00";

        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        // Parsing the time and date into a Date object
        Date eventDate = new Date();
        try{
            eventDate = sdf.parse(str);
        } catch(ParseException e) {
            Log.e("EditEvent", "Error parsing time and date...");
            e.printStackTrace();
        }

        return eventDate;
    }

    private String leftPadDateOrTime(int dateAndTimeDigit) {
        String newPaddedDigit;
        if (dateAndTimeDigit < 10){
            newPaddedDigit = "0" + dateAndTimeDigit;
        }else{
            newPaddedDigit = dateAndTimeDigit + "";
        }
        return newPaddedDigit;
    }

    private void chooseDateButton() {
        final Button dateBtn = (Button) findViewById(R.id.chooseDate);
        final int INITIAL_YEAR = 2015;
        final int INITIAL_MONTH = 0;
        final int INITIAL_DAY = 1;
        //date = {INITIAL_YEAR, INITIAL_MONTH, INITIAL_DAY};

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // When the buttons pressed we pop up a new DatePicker Dialog
                // Parameters: CreateEvent.this : Tells it which Context (Activity) it is in
                //             new DatePickerDialog.OnDateSetListener() : What to do when user clicks done
                //             INITIAL_* : What year, month and day to show when it initially pops up

                new DatePickerDialog(EditEvent.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateBtn.setText(monthOfYear + 1 + "/" + dayOfMonth + "/" + year);       // Months are 0-indexed, Days are 1-indexed
                        date[0] = year;
                        date[1] = monthOfYear;
                        date[2] = dayOfMonth;
                    }
                }, INITIAL_YEAR, INITIAL_MONTH, INITIAL_DAY).show();
            }
        });

        //return dateOfEvent;
    }

}
