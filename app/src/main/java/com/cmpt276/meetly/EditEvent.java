package com.cmpt276.meetly;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class EditEvent extends Activity {
    private final String TAG = "EditEventActivity";

    private Event event;
    private GoogleMap map;
    private EventsDataSource database;
    private Calendar calender = Calendar.getInstance();
    private Integer[] date;
    private Integer[] hourAndMinuteArray;
    private LatLng eventLatLong;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Getting a reference to the event thats passed in
        getEventToEdit();

        // Getting a reference to all our fields
        TextView eventName = (TextView) findViewById(R.id.editEventName);
        TextView eventDate = (TextView) findViewById(R.id.currentEventDate);
        TextView eventDuration = (TextView) findViewById(R.id.editDurationField);

        // Setting the TextViews and Map to display the details of the event thats passed in.
        eventName.setText(event.getTitle());
        eventDate.setText(event.getDate().toString());
        eventDuration.setText("" + event.getDuration());
        Date newDate = event.getDate();
        // TODO: Allow event location to be changed

        // Setting up the map and buttons
        eventLatLong = event.getLocation();
        calender.setTime(event.getDate());
        date = new Integer[]{calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH)};
        hourAndMinuteArray = new Integer[]{calender.get(Calendar.HOUR_OF_DAY), calender.get(Calendar.MINUTE)};

        displayMap();
        chooseDateButton();
        chooseTimeButton();

        // Save Button
        saveButton();

    }

    private void getEventToEdit() {
        Intent in = getIntent();
        Bundle extras = in.getExtras();
        long id = extras.getLong("eventID");

        database = new EventsDataSource(this);
        event = database.findEventByID(id);
    }

    private void saveButton() {
        Button saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText eventName = (EditText) findViewById(R.id.editEventName);
                String newTitle = eventName.getText().toString();

                EditText eventDuration = (EditText) findViewById(R.id.editDurationField);
                int newDuration = Integer.parseInt(eventDuration.getText().toString());

                final Date newDate = formatEventTimeAndDate();

                event.setTitle(newTitle);
                event.setDate(newDate);
                event.setDuration(newDuration);
                event.setLocation(eventLatLong);

                database.updateDatabaseEvent(event);
                finish();
            }
        });
    }

    private void displayMap() {
        // Map Setup
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);

        // Getting the users location and moving our camera there:
        LatLng myLatLng = eventLatLong;

        // Zoom the view into the users location
        final int ZOOM_LEVEL = 12;
        CameraUpdate myLocationCamera = CameraUpdateFactory.newLatLngZoom(myLatLng, ZOOM_LEVEL);
        map.animateCamera(myLocationCamera);

        // Setting a marker to the event location
        map.addMarker(new MarkerOptions().position(myLatLng));

        // Setting a marker to the user selected location
        final ArrayList<LatLng> markerLocation = new ArrayList<LatLng>();     // Work around to having final variables in an inner anon class
        markerLocation.add(0, myLatLng);

        // Making this final to use inside the onMapClick inner class below
        final GoogleMap finalMap = map;

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                finalMap.clear();
                Marker eventMarker = finalMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("Event"));
                markerLocation.add(0, eventMarker.getPosition());
                eventLatLong = markerLocation.get(0);
            }
        });

    }

    private Date formatEventTimeAndDate() {
        // If the month is single digit
        String tempMonth = leftPadDateOrTime(date[1] + 1);

        // If the day is single digit
        String tempDay = leftPadDateOrTime(date[2]);

        // If the hour is single digit
        String tempHour = leftPadDateOrTime(hourAndMinuteArray[0]);

        // If the minute is single digit
        String tempMinute = leftPadDateOrTime(hourAndMinuteArray[1]);

        // yyyy - mm - dd <> hh:mm:ss
        String str = date[0] + "-" + tempMonth + "-" + tempDay + " " + tempHour + ":" + tempMinute + ":" + "00";

        DateFormat sdf = Event.EVENT_SDF;

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
        final Button dateBtn = (Button) findViewById(R.id.editChooseDate);
//        Log.i("Passed in", "y" + calender.get(Calendar.YEAR) + " m" + calender.get(Calendar.MONTH) + " d" + calender.get(Calendar.DAY_OF_MONTH));
        final int INITIAL_YEAR = calender.get(Calendar.YEAR);
        final int INITIAL_MONTH = calender.get(Calendar.MONTH);
        final int INITIAL_DAY = calender.get(Calendar.DAY_OF_MONTH);
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
                        dateBtn.setText(monthOfYear + 1 + "-" + dayOfMonth + "-" + year);       // Months are 0-indexed, Days are 1-indexed
                        date[0] = year;
                        date[1] = monthOfYear;
                        date[2] = dayOfMonth;
//                        Log.i("NewDATE", "" + "y" + date[0] + " m" + date[1] + " d" + date[2]);
                    }
                }, INITIAL_YEAR, INITIAL_MONTH, INITIAL_DAY).show();
            }
        });

    }

    private void chooseTimeButton() {
        final Button timeBtn = (Button) findViewById(R.id.editChooseTime);
        final int INITIAL_HOUR = calender.get(Calendar.HOUR);
        final int INITIAL_MINUTE = calender.get(Calendar.MINUTE);
        hourAndMinuteArray = new Integer[]{INITIAL_HOUR, INITIAL_MINUTE};

        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // When the buttons pressed we pop up a new TimePicker Dialog
                // Parameters: CreateEvent.this : Tells it which Context (Activity) it is in
                //             new TimePickerDialog.OnTimeSetListener() : What to do when user clicks done
                //             INITIAL_HOUR : What hour to show when it initially pops up
                //             INITIAL_MINUTE: What minute to show when it initially pops up
                //             false : Sets displaying the time in a 24 Hour View

                new TimePickerDialog(EditEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                hourAndMinuteArray[0] = hourOfDay;
                                hourAndMinuteArray[1] = minute;

                                String amOrPm = "AM";

                                if (hourOfDay > 12) {
                                    hourOfDay -= 12;
                                    amOrPm = "PM";
                                }
                                String time = String.format("%d : %02d " + amOrPm, hourOfDay, minute);
                                timeBtn.setText(time);
                                Log.i(TAG, "Updated:" + time);
                            }
                        }, INITIAL_HOUR, INITIAL_MINUTE, false).show();
            }
        });

        Log.i(TAG, "Returned:" + hourAndMinuteArray[0] + ":" + hourAndMinuteArray[1]);
    }
}
