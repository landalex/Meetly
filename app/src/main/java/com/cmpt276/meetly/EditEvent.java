package com.cmpt276.meetly;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.GregorianCalendar;

/**
 * This class allows you edit an event thats passed in
 */
public class EditEvent extends ActionBarActivity {
    private final String TAG = "EditEventActivity";

    private Event event;
    private GoogleMap map;
    private EventsDataSource database;
    private Calendar calender = Calendar.getInstance();
    private Calendar endCalender = Calendar.getInstance();
    private Integer[] date;
    private Integer[] hourAndMinuteArray;
    private Integer[] endDate;
    private Integer[] endHourAndMinuteArray;
    private LatLng eventLatLong;
    private Menu actionBarMenu;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Getting a reference to the event thats passed in
        getEventToEdit();

        // Getting a reference to all our fields
        TextView eventName = (TextView) findViewById(R.id.editEventName);
        TextView eventDate = (TextView) findViewById(R.id.currentEventDate);
        //TextView eventDuration = (TextView) findViewById(R.id.editDurationField);

        // Setting the TextViews and Map to display the details of the event thats passed in.
        eventName.setText(event.getTitle());
        eventDate.setText(event.getStartDate().getTime().toString());
        //eventDuration.setText("" + event.getDuration());
        //Date newDate = event.getDate();

        // Setting up the map and buttons
        eventLatLong = event.getLocation();
        calender = event.getStartDate();
        endCalender = event.getEndDate();

        date = new Integer[]{calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH)};
        endDate = new Integer[]{endCalender.get(Calendar.YEAR), endCalender.get(Calendar.MONTH), endCalender.get(Calendar.DAY_OF_MONTH)};
        hourAndMinuteArray = new Integer[]{calender.get(Calendar.HOUR_OF_DAY), calender.get(Calendar.MINUTE)};
        endHourAndMinuteArray = new Integer[]{endCalender.get(Calendar.HOUR_OF_DAY), endCalender.get(Calendar.MINUTE)};

        displayMap();
        chooseDateButton();
        chooseEndDateButton();
        chooseTimeButton();
        chooseEndTimeButton();

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

                //EditText eventDuration = (EditText) findViewById(R.id.editDurationField);
                //int newDuration = Integer.parseInt(eventDuration.getText().toString());

                final Date newDate = formatEventTimeAndDate();
                final Date endDate = formatEndEventTimeAndDate();
                Log.i("DATE DETAILS", newDate.toString() + " END DATE" + endDate.toString());

                event.setTitle(newTitle);

                //Calendar calendar = new GregorianCalendar();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(newDate);
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(endDate);

                event.setStartDate(calendar);
                event.setEndDate(endCalendar);
                //event.setDuration(newDuration);
                event.setLocation(eventLatLong);

                Log.i("NEW DETAILS", event.getStartDate().getTime() + " END DATE" + event.getEndDate().getTime());
                database.updateDatabaseEvent(event);

                //also update event if user is logged in and has shared the event
                SharedPreferences settings = getSharedPreferences(Meetly.MEETLY_PREFERENCES, MODE_PRIVATE);
                boolean loggedIn = settings.getBoolean(Meetly.MEETLY_PREFERENCES_ISLOGGEDIN, false);

                if(loggedIn && event.getSharedEventID() != -1){
                    MeetlyServer server = new MeetlyServer();

                    try{
                        server.publishEvent(
                                settings.getString(Meetly.MEETLY_PREFERENCES_USERNAME, Meetly.defaultUMessage),
                                settings.getInt(Meetly.MEETLY_PREFERENCES_USERTOKEN, -1),
                                event.getTitle(),
                                event.getStartDate(),
                                event.getEndDate(),
                                event.getLocation().latitude,
                                event.getLocation().longitude);
                    }catch (MeetlyServer.FailedPublicationException e) {
                        e.printStackTrace();
                    }

                }
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
        String str = date[0] + "/" + tempMonth + "/" + tempDay + " " + tempHour + ":" + tempMinute + ":" + "00";

        DateFormat sdf = Event.EVENT_DATEFORMAT;

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
                        dateBtn.setText(monthOfYear + 1 + "/" + dayOfMonth + "/" + year);       // Months are 0-indexed, Days are 1-indexed
                        date[0] = year;
                        date[1] = monthOfYear;
                        date[2] = dayOfMonth;
//                        Log.i("NewDATE", "" + "y" + date[0] + " m" + date[1] + " d" + date[2]);
                    }
                }, INITIAL_YEAR, INITIAL_MONTH, INITIAL_DAY).show();
            }
        });

    }

    private void chooseEndDateButton() {
        final Button dateBtn = (Button) findViewById(R.id.editChooseEndDate);
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

                new DatePickerDialog(EditEvent.this, new DatePickerDialog.OnDateSetListener() {
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

    private void chooseEndTimeButton() {
        final Button timeBtn = (Button) findViewById(R.id.editChooseEndTime);
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

                new TimePickerDialog(EditEvent.this,
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_event, menu);
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
