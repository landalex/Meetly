package com.cmpt276.meetly;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Fields: Name, Location (Open map), Date/Time (Use the circle thingy), People (Open contacts), Notes (1000 characters?)
 */
public class CreateEvent extends Activity { //extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = "CreateEventActivity";
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        chooseTimeButton();
        chooseDateButton();

        // Map
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);


        // Getting the users location and moving our camera there:

        // Instantiate a LocationManager.
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);

        // Criteria specifies the 'criteria' of how granular the location is
        Criteria criteria = new Criteria();

        // Get the name of the best provider. AKA Returns the name of the provider that best meets the given criteria.
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);

        // Store current location as a Latitude and Longitude
        LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

        // Zoom the view into the users location
        CameraUpdate myLocationCamera = CameraUpdateFactory.newLatLngZoom(myLatLng, 12);
        map.animateCamera(myLocationCamera);


        // Setting a marker to the user selected location
        final ArrayList<LatLng> markerLocation = new ArrayList<>();     // Work around to having final variables in an inner anon class
        markerLocation.add(0, myLatLng);

        // Making this final to use inside the onMapClick inner class below
        final GoogleMap finalMap = map;

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                finalMap.clear();
                Marker eventMarker = finalMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("Event"));
                markerLocation.add(0, eventMarker.getPosition());
            }
        });

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
        final int INITIAL_YEAR = 2015;
        final int INITIAL_MONTH = 0;
        final int INITIAL_DAY = 1;

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
                    }
                }, INITIAL_YEAR, INITIAL_MONTH, INITIAL_DAY).show();
            }
        });
    }


}
