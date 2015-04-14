package com.cmpt276.meetly;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.r0adkll.slidr.Slidr;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Fields: Name, Location, Date/Time, Time to event start, People, Notes,
 */
public class ViewEvent extends ActionBarActivity {

    private final String TAG = "ViewEventActivity";
    private Event thisEvent;
    private Long eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        int primary = getResources().getColor(R.color.green_dark);
        int secondary = getResources().getColor(R.color.blue_dark);
        Slidr.attach(this, primary, secondary);

        getEventToView();
        showEvent();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            Intent intent = new Intent(ViewEvent.this, EditEvent.class);
            intent.putExtra("eventID", eventID);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * Sends event details to view screen to display to user
     */
    private void showEvent(){
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(thisEvent.getTitle());

        textView = (TextView) findViewById(R.id.date);
        textView.setText("Starts at: " + Event.getTimestringForEventStart(thisEvent) + " and ends at: " + Event.getTimestringForEventEnd(thisEvent));

        textView = (TextView) findViewById(R.id.location);

        textView.setText(thisEvent.getLocation().toString());

        textView = (TextView) findViewById(R.id.location);
        String tempString = thisEvent.getLocation().toString();
        textView.setText(tempString);
    }


    private String timeUntil(Calendar startDate) {
        long now = new Date().getTime();
        long eventTime = startDate.getTimeInMillis();
        long diff = eventTime - now;

        if (diff <= 0) {
            return "Happening now";
        }

        final long hoursInDay = TimeUnit.DAYS.toHours(1);
        final long minutesInHour = TimeUnit.HOURS.toMinutes(1);

        long daysUntil = TimeUnit.MILLISECONDS.toDays(diff);
        long hoursUntil = TimeUnit.MILLISECONDS.toHours(diff) % hoursInDay;
        long minutesUntil = TimeUnit.MILLISECONDS.toMinutes(diff) % minutesInHour;

        return String.format("Happening in %02d days, %02d hours, and %02d minutes", daysUntil, hoursUntil, minutesUntil);
    }

    /*
     * Retrieves event passed by previous activity
     */
    private void getEventToView(){
        Intent in = getIntent();
        Bundle extras = in.getExtras();
        eventID = extras.getLong("eventID");
        Log.i(TAG, "" + eventID);
        EventsDataSource eds = new EventsDataSource(getApplicationContext());
        thisEvent = eds.findEventByID(eventID);

        //thisEvent.printEventS();
    }
}
