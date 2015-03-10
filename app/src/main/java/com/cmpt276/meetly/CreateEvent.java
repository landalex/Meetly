package com.cmpt276.meetly;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

/**
 * Fields: Name, Location (Open map), Date/Time (Use the circle thingy), People (Open contacts), Notes (1000 characters?)
 */
public class CreateEvent extends Activity {

    private final String TAG = "CreateEventActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        chooseTimeButton();
        chooseDateButton();
    }

    private void chooseTimeButton() {
        final Button timeBtn = (Button) findViewById(R.id.chooseTime);
        final int INITIAL_HOUR = 8;
        final int INITIAL_MINUTE = 0;

        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // When the buttons pressed we pop up a new TimePicker Dialog
                // Parameters: CreateEvent.this : Tells it which Context (Activity) it is in
                //             new TimePickerDialog.OnTimeSetListener() : What to do when user clicks done
                //             INITIAL_HOUR : What hour to show when it initially pops up
                //             INITIAL_MINUTE: What minute to show when it initially pops up
                //             false : Sets displaying the time in a 24 Hour View

                new TimePickerDialog(CreateEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String amOrPm = "AM";

                                if (hourOfDay > 12) {
                                    hourOfDay -= 12;
                                    amOrPm = "PM";
                                }
                                String time = String.format("%d : %02d " + amOrPm, hourOfDay, minute);
                                timeBtn.setText(time);
                            }
                        }, INITIAL_HOUR, INITIAL_MINUTE, false).show();
            }
        });
    }

    private void chooseDateButton() {
        final Button dateBtn = (Button) findViewById(R.id.chooseDate);

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DatePickerDialog(CreateEvent.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateBtn.setText("" + monthOfYear + "/" + dayOfMonth + "/" + year);
                    }
                }, 2015, 2, 1).show();
            }
        });
    }

}
