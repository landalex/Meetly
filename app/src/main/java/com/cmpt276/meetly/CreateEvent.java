package com.cmpt276.meetly;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Allows user to specify details about an event, and create the event in the database
 */

public class CreateEvent extends ActionBarActivity {

    private final String TAG = "CreateEventActivity";
    private GoogleMap map;
    private Integer[] hourAndMinuteArray = new Integer[]{0, 0};
    private Integer[] endHourAndMinuteArray = new Integer[]{0, 0};
    private Integer[] date = new Integer[]{2015, 1, 1};
    private Integer[] endDate = new Integer[]{2015, 1, 2};
    private LatLng eventLatLong = new LatLng(49.176872923625645, -122.8456462919712);      // Intersection of King George and 96
    private Menu actionBarMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Getting info from buttons and map
        chooseTimeButton();
        chooseEndTimeButton();
        chooseDateButton();
        chooseEndDateButton();
        displayMap();

        // Getting name and duration from their fields
        final EditText eventNameField = (EditText) findViewById(R.id.eventName);
        //final EditText durationField = (EditText) findViewById(R.id.durationField);

        // Getting a reference to the submit button
        submitButton(eventNameField);

    }

    private void submitButton(final EditText eventNameField) {
        Button submitBtn = (Button) findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Date finalEventDate = formatEventTimeAndDate();
                final Date finalEndEventDate = formatEndEventTimeAndDate();

                // Making Calendar objects
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(finalEventDate);

                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(finalEndEventDate);

                //endCalendar.add(Calendar.HOUR, Integer.parseInt(durationField.getText().toString()));

                EventsDataSource event = new EventsDataSource(CreateEvent.this);

                event.createEvent(eventNameField.getText().toString(), startCalendar, endCalendar, eventLatLong);

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
        DateFormat sdf = Event.EVENT_DATEFORMAT;

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

    private Date formatEndEventTimeAndDate() {
        // If the month is single digit
        String tempMonth = leftPadDateOrTime(endDate[1] + 1);

        // If the day is single digit
        String tempDay = leftPadDateOrTime(endDate[2]);

        // If the hour is single digit
        String tempHour = leftPadDateOrTime(endHourAndMinuteArray[0]);

        // If the minute is single digit
        String tempMinute = leftPadDateOrTime(endHourAndMinuteArray[1]);

        // yyyy - mm - dd <> hh:mm:ss
        String str = endDate[0] + "/" + tempMonth + "/" + tempDay + " " + tempHour + ":" + tempMinute + ":" + "00";
        DateFormat sdf = Event.EVENT_DATEFORMAT;

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

    private void chooseEndTimeButton() {
        final Button timeBtn = (Button) findViewById(R.id.chooseEndTime);
        final int INITIAL_HOUR = 12;
        final int INITIAL_MINUTE = 55;
        endHourAndMinuteArray = new Integer[]{INITIAL_HOUR, INITIAL_MINUTE};

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
                                endHourAndMinuteArray[0] = hourOfDay;
                                endHourAndMinuteArray[1] = minute;

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

        Log.i(TAG, "Returned:" + endHourAndMinuteArray[0] + ":" + endHourAndMinuteArray[1]);
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

    private void chooseEndDateButton() {
        final Button dateBtn = (Button) findViewById(R.id.chooseEndDate);
        final int INITIAL_YEAR = 2015;
        final int INITIAL_MONTH = 0;
        final int INITIAL_DAY = 2;

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
                        endDate[0] = year;
                        endDate[1] = monthOfYear;
                        endDate[2] = dayOfMonth;
                    }
                }, INITIAL_YEAR, INITIAL_MONTH, INITIAL_DAY).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_event, menu);
        actionBarMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            findViewById(R.id.submitBtn).callOnClick();
        }
        else if (id == R.id.action_discard) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.activity_create_discard_dialog_title));
            builder.setMessage(getString(R.string.activity_create_discard_dialog_message));
            builder.setPositiveButton(getString(R.string.activity_create_discard_dialog_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            builder.setNegativeButton(getString(R.string.activity_create_discard_dialog_negative), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * Changes the menu_login menu item text depending if user is logged in or not
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


}
