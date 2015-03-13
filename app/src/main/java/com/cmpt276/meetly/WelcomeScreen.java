package com.cmpt276.meetly;

import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Animation, ends with start screen (View or create events)
 */
public class WelcomeScreen extends ActionBarActivity implements Animation.AnimationListener {
    private static final int TAGLINE_DELAY = 1500;
    private final String TAG = "WelcomeScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        playImageAnimation(R.id.indicator, R.anim.fade_in_animation);
        playTextAnimation(R.id.appName);
        animationStartDelay(R.id.tagline, R.anim.fade_in_animation, TAGLINE_DELAY);
    }

    private void playTextAnimation(int id) {
        TextView text = (TextView)findViewById(id);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);

        text.setVisibility(View.VISIBLE);
        text.startAnimation(animation);
    }

    private void playImageAnimation(int id, int animationId) {
        View view = (View) findViewById(id);
        Animation animation = AnimationUtils.loadAnimation(this, animationId);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(animation);
    }

    private void animationStartDelay(int id, int animationId, int delay){
        TextView text = (TextView)findViewById(id);
        Animation animation = AnimationUtils.loadAnimation(this, animationId);
        animation.setStartOffset(delay);
        text.setVisibility(View.VISIBLE);
        text.startAnimation(animation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome_screen, menu);
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

    public void onSkipIntro(View view){
        Log.i(TAG, "Skipping intro");

        // Launch MainActivity for testing EventList and EventsDataSource
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
