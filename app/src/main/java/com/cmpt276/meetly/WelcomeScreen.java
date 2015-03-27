package com.cmpt276.meetly;

import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Displays the start animation and logo
 * User taps anywhere to skip splash screen
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
    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                // Finger touches the screen
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();

            case MotionEvent.ACTION_MOVE:
                // Finger moves on the screen
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
        }

        return super.onTouchEvent(event);
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
