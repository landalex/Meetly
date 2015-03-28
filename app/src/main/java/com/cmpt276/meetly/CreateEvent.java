package com.cmpt276.meetly;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Allows user to specify details about an event, and create the event in the database
 */

public class CreateEvent extends Activity {

    private final String TAG = "CreateEventActivity";
    private GoogleMap map;
    private Integer[] hourAndMinuteArray = new Integer[]{0, 0};
    private Integer[] date = new Integer[]{2015, 1, 1};
    private LatLng eventLatLong = new LatLng(49.176872923625645, -122.8456462919712);      // Intersection of King George and 96

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Getting info from buttons and map
        chooseTimeButton();
        chooseDateButton();
        displayMap();

        // Getting name and duration from their fields
        final EditText eventNameField = (EditText) findViewById(R.id.eventName);
        final EditText durationField = (EditText) findViewById(R.id.durationField);

        // Getting a reference to the submit button
        submitButton(eventNameField, durationField);

    }

    private void submitButton(final EditText eventNameField, final EditText durationField) {
        Button submitBtn = (Button) findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Date finalEventDate = formatEventTimeAndDate();

                EventsDataSource event = new EventsDataSource(CreateEvent.this);
                event.createEvent(eventNameField.getText().toString(), finalEventDate, eventLatLong, Integer.parseInt(durationField.getText().toString()));
                Log.i("Final Event Going in: ", finalEventDate.toString());
                finish();
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
        String str = date[0] + "/" + tempMonth + "/" + tempDay + " " + tempHour + ":" + tempMinute + ":" + "00";
        DateFormat sdf = Event.EVENT_SDF;

        // Parsing the time and date into a Date object
        Date eventDate = new Date();
        try{
            eventDate = sdf.parse(str);
        } catch(ParseException e) {
            Log.e(TAG, "Error parsing time and date...");
            e.printStackTrace();
        }

        Log.i("Date", eventDate.toString());
        Log.i("SDF ", str);
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


    private void displayMap() {
        // Map Setup
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);

        // Getting the users location and moving our camera there:
        LatLng myLatLng = getCurrentLocation();

        // Zoom the view into the users location
        final int ZOOM_LEVEL = 11;
        CameraUpdate myLocationCamera = CameraUpdateFactory.newLatLngZoom(myLatLng, ZOOM_LEVEL);
        map.animateCamera(myLocationCamera);


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

    private LatLng getCurrentLocation() {
        // Instantiate a LocationManager.
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);

        // Criteria specifies the 'criteria' of how granular the location is
        // Get the name of the best provider. AKA Returns the name of the provider that best meets the given criteria.
        String provider = locationManager.getBestProvider(new Criteria(), true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);

        // If the GPS is turned off return a default value.
        if (myLocation == null) {
            return new LatLng(49.176872923625645, -122.8456462919712);      // Intersection of King George and 96
        } else {
            return new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        }
    }

    private void chooseTimeButton() {
        final Button timeBtn = (Button) findViewById(R.id.chooseTime);
        final int INITIAL_HOUR = 12;
        final int INITIAL_MINUTE = 45;
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

                new TimePickerDialog(CreateEvent.this,
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
//        return hourAndMinuteArray;
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

                new DatePickerDialog(CreateEvent.this, new DatePickerDialog.OnDateSetListener() {
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
