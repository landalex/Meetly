package com.cmpt276.meetly;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Fields: Name, Location, Date/Time, Time to event start, People, Notes,
 */
public class ViewEvent extends ActionBarActivity {

    private final String TAG = "ViewEventActivity";
    private Event thisEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sends event details to view screen to display to user
     */
    private void showEvent(){
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(thisEvent.getTitle());

        textView = (TextView) findViewById(R.id.date);
        textView.setText(thisEvent._12HRgetDate());

        textView = (TextView) findViewById(R.id.location);

        textView.setText(thisEvent.getLocation().toString());

        textView = (TextView) findViewById(R.id.attendees);
        String tempString = thisEvent.getAttendees().toString();
        tempString = tempString.substring(1,tempString.length()-1);
        textView.setText(tempString);

        textView = (TextView) findViewById(R.id.duration);
        textView.setText(thisEvent.getDuration());
    }

    /**
     * Retrieves event passed by previous activity
     */
    private void getEventToView(){
        Intent in = getIntent();
        Bundle extras = in.getExtras();
        long id = extras.getLong("eventID");

        EventsDataSource eds = new EventsDataSource(getApplicationContext());
        thisEvent = eds.findEventByID(id);
        thisEvent.printEventS();
    }
}
