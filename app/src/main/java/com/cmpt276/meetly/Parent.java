package com.cmpt276.meetly;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cmpt276.meetly.R;

public class Parent extends ActionBarActivity {

    private final String TAG = "ParentActivity";

    //Databse properties
    public final String DB_NAME = "MeetlyDB";
    public final String DB_PATH = "com.cmpt276.meetly.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_parent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_parent, menu);
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

    /*
    This Section will be for wrapper methods for using sql querys
     */

    /**
     * Creates application database
     * @return int --> Success == 0, Failure == -1
     */
    private int createDatabase(){

        return -1;
    }

    public String[] getFirstRecord(){
        String[] record = {"",""};
        return record;
    }

    public String[] getRecordAt(){
        String[] record = {"",""};
        return record;
    }

    public String[] getRecordMatch(){
        String[] record = {"",""};
        return record;
    }
}



























