package com.example.compassapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayCompassActivity extends AppCompatActivity implements SensorEventListener{

    // compass ImageView
    private ImageView image;

    // The current picture angle from original pos
    private float currentDegree = 0F;

    // The handler for the device sensors
    private SensorManager sensorManager;

    TextView degreeTextView;

    //

    private float[] prevAccReading = new float[3];
    private float[] prevMagReading = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_compass);

        // retrives the image view for the compass
        image = (ImageView) findViewById(R.id.compassImageView);

        // TextView that will display the degree
        degreeTextView = (TextView) findViewById(R.id.degreeTextView);

        // initializes the android devices sensor handler/manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        sensorManager.unregisterListener(this);
        System.out.println("inside onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // code for system's orientation sensor registered listeners
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_GAME);
        //System.out.println("inside onResume");
        //sensorManager.registerListener(this,  sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        //        SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        System.out.println("event values: " + Math.toDegrees(event.values[2]));
        float degree = Math.round(Math.toDegrees(event.values[2]));

        String newDegStr = "Heading: " + Float.toString(degree) + " degrees";
        degreeTextView.setText(newDegStr);

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Method not used but needed when implementing SensorEventListener
    }
}