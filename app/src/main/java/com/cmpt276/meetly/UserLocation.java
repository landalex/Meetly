package com.cmpt276.meetly;

import android.app.Activity;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import java.util.List;

/**
 * Creates a crouton notification to display user location
 */
public class UserLocation {

    protected static String getLocation(Activity activity) {
        String location = activity.getString(R.string.no_location_found);

        LocationManager locationManager = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        Location myLocation = locationManager.getLastKnownLocation(provider);
        Geocoder geocoder = new Geocoder(activity);
        try {
            List<Address> addresses = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 5);
            location = addresses.get(0).getLocality();
        }
        catch (Exception e) {
            Log.e("myLocation", "geocoder.getFromLocation failed");
        }
        return location;
    }
}
